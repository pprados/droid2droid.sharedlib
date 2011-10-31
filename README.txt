Two mode of build:
- Without shared library
- With shared library

The first mode is easier for develop and debug. The second is better to distribute.

* Without shared library
- Update the project builder, and uncheck the Ant builder
- Update the flag org.remoteandroid.RemoteAndroidManager.USE_SHAREDLIB to false
- For project, add a dependencies to androids project remote-android-lib and remote-android-sharedlib

* With shared library
- Update the project build and check the Ant builder
- Update the flag org.remoteandroid.RemoteAndroidManager.USE_SHAREDLIB to true
- For project, add a dependencies to android project remote-android-lib and not to remote-android-sharedlib

To build the project for distribution, you must have Ant >1.8
Create a local.properties files in root directory of the project with something like that:
------------------------------
# Adapte the file to your context
sdk.dir=/home/pprados/bin/android-sdk
android.libraries.src=${sdk.dir}/
key.store=/home/USERNAME/.android/debug.keystore
key.alias=androiddebugkey
key.store.password=android
key.alias.password=android
------------------------------