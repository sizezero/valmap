# ValMap

## Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).

The program reads a csv file that contains map coordinates and creates a pdf in the project's `out/` dirctory. You can either specify a single file or a directory to convert multiple files.

The CSV file consists of two sections: a properties section that has names and values and a "Locations" section that contains a type of Glyph to render and an X/Y coordinate in meters.

[`stonia.csv`](samples/stonia.csv) is a sample file in the project that you can run with:

    sbt 'run samples/stonia.csv'

## Use Case

I decided to play a run of [Valheim](https://www.valheimgame.com/) in Immersive mode which, among other things means no maps and no portals. The gloal of this run is to build and east/west road that spans all the equatorial continents (with gaps on the oceans between them) and a north/south road that hits the Ashlands in the south and the Deep North. The roads meet at the starting stones.

### General Directions

Like any other world the sun rises in the east and sets in the west. On a clear morning or evening this is helpful for general directions.

The sky contains the [Ygdrasil World Tree](https://www.reddit.com/r/valheim/comments/17pnu04/yggdrasil_overlaid_on_the_map/) whose trunk starts in the east and fans out into the west.

Raising the ground from a single spot creates an obilisk that is guaranteed to have four faces in each of the four cardinal directions. On a clear morning and evening it is easy to tell east from west so I stuck a board in the north side to make them useful on cloudy days. I made a lot of these and called them compasses.

### Directions

Pulling out the hammer and selecting an item allows you to rotated it in one of 16 degrees. If you align the item with a compass you have a way to see the direction as you walk. The bed is a nice item as it is big and has a headboard that you can use for north.

The cooking station is kind of nice to because you can build it without a workbench so you can easily stick on on the ground to remember a direction. The cooking station also has "sights" that allow you to use it like a rifle to take a bearing on distant objects.

Using these techniques it was fairly easy to start the road from the stones. It only required a lot of tree chopping and a little bridge building when a river was encountered. I don't really have a method of determining how accurate the road is since there could have been some drift. I just have to hope that left and right drift evens out.

### Distances

There are three basic ways to measure distances.

**foo**

    1. __Build floorboard or horizontal beams.__ This is the most cumbersome but the most accurate. I only use it for very short distances less then ten meters or so.
    2. **Trot in a straight line and time your walk with a stopwatch.** I built a 100m race track and determined that it consistently takes 26 seconds to trot it in troll armor while carrying the hammer. This is 3.8 m/s. I can use this to calculate distances fairly easily and accurately.
    3. **Estimate.** Just look into the distance and guess. This is the least accurate method but sometimes (like looking over a canyon) there is no alternative.