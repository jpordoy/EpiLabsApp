import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// Initialize Firebase Admin
admin.initializeApp();
const db = admin.firestore();

// Mock Twilio for emulator
const isEmulator = process.env.FUNCTIONS_EMULATOR === 'true';

// Mock Twilio client for emulator
const mockTwilioClient = {
  messages: {
    create: async (messageData: any) => {
      console.log('📱 MOCK SMS SENT:', {
        to: messageData.to,
        from: messageData.from,
        body: messageData.body
      });
      return {
        sid: `mock_${Date.now()}`,
        status: 'sent',
        to: messageData.to,
        from: messageData.from
      };
    }
  }
};

// Real Twilio client (for production)
let twilioClient: any;
if (!isEmulator) {
  const twilio = require('twilio');
  const accountSid = functions.config().twilio?.account_sid;
  const authToken = functions.config().twilio?.auth_token;
  twilioClient = twilio(accountSid, authToken);
} else {
  twilioClient = mockTwilioClient;
}

// Send SMS (works with both real Twilio and mock)
export const sendTwilioSms = functions.https.onCall(async (data: any, context: functions.https.CallableContext) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { to, from, body } = data;

  try {
    const message = await twilioClient.messages.create({
      body: body,
      from: from || '+1234567890', // Mock number for emulator
      to: to
    });

    console.log('SMS sent successfully:', message.sid);
    return { success: true, messageSid: message.sid };
  } catch (error) {
    console.error('Error sending SMS:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send SMS');
  }
});

// Handle seizure detection with enhanced logging
export const handleSeizureDetection = functions.https.onCall(async (data: any, context: functions.https.CallableContext) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { userId, timestamp, confidence } = data;
  console.log(`🚨 SEIZURE DETECTED for user ${userId} with confidence ${confidence}`);

  try {
    // Get user's emergency contacts
    const contactsSnapshot = await db.collection('contacts')
      .where('userId', '==', userId)
      .get();

    const userData = (await db.collection('users').doc(userId).get()).data();
    const userName = userData ? `${userData.firstName} ${userData.lastName}` : 'EpiGuard User';

    console.log(`👤 User: ${userName}, 📞 Contacts: ${contactsSnapshot.size}`);

    // Send SMS to all contacts
    const smsPromises: Promise<any>[] = [];

    contactsSnapshot.forEach((doc) => {
      const contact = doc.data();

      const smsMessage = `🚨 SEIZURE ALERT: ${userName} may be having a seizure at ${new Date(timestamp).toLocaleString()}. Confidence: ${Math.round(confidence * 100)}%. Please check on them immediately.`;

      console.log(`📱 Sending SMS to: ${contact.contactNumber}`);

      smsPromises.push(
        twilioClient.messages.create({
          body: smsMessage,
          from: isEmulator ? '+1234567890' : functions.config().twilio?.from_number,
          to: contact.contactNumber
        })
      );
    });

    // Execute all SMS sends
    const results = await Promise.all(smsPromises);

    // Log results
    results.forEach((result, index) => {
      console.log(`✅ SMS ${index + 1} result:`, result);
    });

    // Save seizure event to Firestore
    await db.collection('seizures').add({
      userId: userId,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      confidence: confidence,
      contactsNotified: contactsSnapshot.size,
      detectionTimestamp: timestamp
    });

    console.log(`✅ Seizure alert completed for user ${userId}`);
    return {
      success: true,
      contactsNotified: contactsSnapshot.size,
      isEmulator: isEmulator
    };

  } catch (error) {
    console.error('❌ Error handling seizure detection:', error);
    throw new functions.https.HttpsError('internal', 'Failed to handle seizure detection');
  }
});

// Send push notifications (mock for emulator)
export const sendPushNotification = functions.https.onCall(async (data: any, context: functions.https.CallableContext) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { userId, title, body, type = 'GENERAL' } = data;

  try {
    if (isEmulator) {
      console.log('📢 MOCK PUSH NOTIFICATION:', {
        userId,
        title,
        body,
        type
      });

      // Save notification to Firestore
      await db.collection('notifications').add({
        userId: userId,
        title: title,
        message: body,
        type: type,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isRead: false
      });

      return { success: true, message: 'Mock push notification sent' };
    }

    // Real push notification logic for production
    const userDoc = await db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'User not found');
    }

    const userData = userDoc.data();
    const fcmTokens = userData?.fcmTokens || [];

    if (fcmTokens.length === 0) {
      return { success: false, message: 'No FCM tokens found' };
    }

    const payload = {
      notification: { title, body },
      data: { type, userId }
    };

    const response = await admin.messaging().sendToDevice(fcmTokens, payload);

    await db.collection('notifications').add({
      userId,
      title,
      message: body,
      type,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isRead: false
    });

    return { success: true, results: response.results };
  } catch (error) {
    console.error('Error sending push notification:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification');
  }
});

// Test function for emulator
export const testEmulator = functions.https.onCall(async (data: any, context: functions.https.CallableContext) => {
  return {
    message: 'Emulator is working!',
    timestamp: new Date().toISOString(),
    isEmulator: process.env.FUNCTIONS_EMULATOR === 'true'
  };
});