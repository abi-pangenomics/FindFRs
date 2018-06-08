# FindFRs

## Building

You can build the project with [ant](http://ant.apache.org/).
The project dependencies are managed with [ivy](http://ant.apache.org/ivy/).  If
you don't have ivy, don't worry, the build process will automatically set up ivy
provided that you are online.

Ant is kind of like the Unix program `make`. There are a lot of build options,
but the most useful are to build the project run:

    ant

This will produce a collection of build artifacts in the `dist` folder.  To run
`FindFRs` from the project root, run

    java -jar dist/FindFRs.jar ...with FindFRs args...

To clean up build artifacts and temporary files run.

    ant clean

