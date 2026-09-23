import re

with open("wear/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

srv_old = """    </application>"""

srv_new = """        <service
            android:name=".VedicComplicationDataSourceService"
            android:exported="true"
            android:label="Vedic Tithi &amp; Nakshatra"
            android:permission="com.google.android.wearable.permission.BIND_COMPLICATION_PROVIDER">
            <intent-filter>
                <action android:name="android.support.wearable.complications.ACTION_COMPLICATION_UPDATE_REQUEST" />
            </intent-filter>
            
            <meta-data
                android:name="android.support.wearable.complications.SUPPORTED_TYPES"
                android:value="SHORT_TEXT,LONG_TEXT" />
            
            <meta-data
                android:name="android.support.wearable.complications.UPDATE_PERIOD_SECONDS"
                android:value="3600" />
        </service>
    </application>"""

content = content.replace(srv_old, srv_new)

with open("wear/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
