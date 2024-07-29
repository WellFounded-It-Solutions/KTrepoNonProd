# Releasing

1. Update the `VERSION_NAME` in `gradle.properties` to the release version.

2. Commit

   ```
   $ git commit -am "[Release] Prepare version X.Y.X"
   ```

3.Tag

   ```
   $ git tag -am "[Release] Version X.Y.Z" X.Y.Z
   ```

4.Update the `VERSION_NAME` in `gradle.properties` to the next "SNAPSHOT" version.

5.Commit

   ```

      $ git commit -am "[Release] Prepare next development iteration"
   ```

7. Push!

   ```
   $ git push && git push --tags
   ```

// TODO This could probably be accomplished with a convention plugin ;)