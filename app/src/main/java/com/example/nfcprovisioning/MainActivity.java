package com.example.nfcprovisioning;

import android.app.admin.DevicePolicyManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

	private NfcAdapter mNfcAdapter;

	private final NdefMessageCallback mNdefMessageCallback = new NdefMessageCallback();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null) {
			mNfcAdapter.setNdefPushMessageCallback(mNdefMessageCallback, this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mNfcAdapter.setNdefPushMessageCallback(null, this);
	}

	private class NdefMessageCallback implements NfcAdapter.CreateNdefMessageCallback {
		@Override
		public NdefMessage createNdefMessage(final NfcEvent event) {
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			final Properties properties = new Properties();

			properties.put(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, "com.example.deviceowner");

			// Make sure to put local time in the properties. This is necessary on some devices to
			// reliably download the device owner APK from an HTTPS connection.
			properties.put(
					DevicePolicyManager.EXTRA_PROVISIONING_LOCAL_TIME,
					String.valueOf(System.currentTimeMillis())
			);
			// To calculate checksum execute command (taken from http://stackoverflow.com/questions/26509770/checksum-error-while-provisioning-android-lollipop):
			// cat Something.apk | openssl dgst -binary -sha1 | openssl base64 | tr '+/' '-_' | tr -d '='
			properties.put(
					DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM,
					"i5LG2lOUBZa46T3lk54Uk_1G4EU"
			);
			properties.put(
					DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION,
					"http://igor-lapin.ru/do.apk"
			);

			try {
				properties.store(outputStream, getString(R.string.nfc_comment));
				final NdefRecord record = NdefRecord.createMime(
						DevicePolicyManager.MIME_TYPE_PROVISIONING_NFC,
						outputStream.toByteArray()
				);

				return new NdefMessage(new NdefRecord[]{record});
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
