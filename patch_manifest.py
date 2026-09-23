with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

widget_decl = """        <receiver android:name=".ui.widgets.VedicAppWidgetProvider" android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/vedic_appwidget_info" />
        </receiver>

        <service android:name=".ui.watch.CosmicScoreComplicationService"
                 android:exported="true"
                 android:permission="com.google.android.wearable.permission.BIND_COMPLICATION_PROVIDER">
            <intent-filter>
                <action android:name="android.support.wearable.complications.ACTION_COMPLICATION_UPDATE_REQUEST" />
            </intent-filter>
            <meta-data android:name="android.support.wearable.complications.SUPPORTED_TYPES" android:value="SHORT_TEXT,LONG_TEXT" />
            <meta-data android:name="android.support.wearable.complications.UPDATE_PERIOD_SECONDS" android:value="3600" />
        </service>"""

content = content.replace('        </activity>', '        </activity>\n' + widget_decl)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
