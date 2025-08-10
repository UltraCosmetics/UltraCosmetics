# Compiling

This project depends on NMS, which shouldn't be redistributed. This means you'll have to build some of the dependencies
yourself. However, there's a task built into UC's Gradle script to do this automatically.

All you need to do to compile is run `./gradlew prepareDependencies build`. It's not uncommon for it to take 15
minutes the first time, since it needs to build two versions of Spigot. When it's done, you can find the resulting jar
at `build/libs/UltraCosmetics-<version>-<buildtype>.jar`. Subsequent builds should take less than a minute.

And you're done!
