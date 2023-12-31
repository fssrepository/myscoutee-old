cd /your_home/fms

ionic build android --prod --release

cd /your_home/Android/Sdk/build-tools/25.0.3/

cp /xour_home/fms/platforms/android/build/outputs/apk/android-release-unsigned.apk .

./zipalign -v -f 4 android-release-unsigned.apk android-release-unsigned-aligned.apk
./apksigner sign --ks my-release-key.jks --out android-release.apk android-release-unsigned-aligned.apk

