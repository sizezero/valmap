# ValMap

## Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

To install sbt (and Scala) see the [Scala Install Page](https://www.scala-lang.org/download/)

The program reads a csv file that contains map coordinates and creates a pdf in the project's `out/` dirctory. You can either specify a single file or a directory to convert multiple files.

The CSV file consists of two sections: a properties section that has names and values and a "Locations" section that contains a type of Glyph to render and an (X,Y) coordinate in meters.

[`stonia.csv`](samples/stonia.csv) is a sample file in the project that you can run with:

    sbt 'run samples/stonia.csv'

## Use Case

I decided to play a run of [Valheim](https://www.valheimgame.com/) in Immersive mode which, among other things means no maps and no portals. The goal of this run is to build an east/west road that spans all the equatorial continents (with gaps on the oceans between them) and a north/south road that hits the Ashlands in the south and the Deep North. The roads meet at the starting stones.

### General Orientation

Like our world, in the world of Valheim the sun rises in the east and sets in the west. On a clear morning or evening this is helpful for general directions.

The sky contains the [Ygdrasil World Tree](https://www.reddit.com/r/valheim/comments/17pnu04/yggdrasil_overlaid_on_the_map/) whose trunk starts in the east and fans out into the west. This can be help orient you in the middle of a clear day.

![world tree](http://kleemann.org/valheim/valmap-readme/world-tree.png)

Raising the ground from a single spot creates an obilisk that is guaranteed to have four faces in each of the four cardinal directions. On a clear morning and evening it is easy to tell east from west so I stuck a board in the north side to make them useful on cloudy days. I made a lot of these and called them compasses.

![Compass](http://kleemann.org/valheim/valmap-readme/compass.png)

### Maintaining Direction

Pulling out the hammer and selecting an item allows you to rotate it in one of 16 directions. Four of these directions exactly align with cardinal directions, four with intercardinal directions, and the remaining eight with the secondary intercardinals. If you align the item with a compass you have a way to see the direction as you walk. If you switch tools and go back to the hammer, the selected item maintains it's previous direction. The bed is a nice item as it is big and has a headboard that you can use for north.

![Bed](http://kleemann.org/valheim/valmap-readme/bed.png)

The cooking station is kind of nice too because you can build it without a workbench so you can easily stick on on the ground to remember a direction. The cooking station also has "sights" that allow you to use it like a rifle to take a bearing on distant objects. MOBs like to break cooking stations so build a compass if you want a permanent marker.

![Cooking Station Sights](http://kleemann.org/valheim/valmap-readme/cooking-station-sights.png)

Using these techniques it was fairly easy to build the road from the starting stones. It only required a lot of tree chopping and a little bridge building when a river was encountered. I don't really have a method of determining how accurate the road is since there could have been some drift. I just have to hope that left and right drift evens out.

![Long Road](http://kleemann.org/valheim/valmap-readme/long-road.png)

### Distances

There are three basic ways to measure distances.

1. **Build floorboard or horizontal beams.** This is the most cumbersome but the most accurate. I only use it for very short distances less then ten meters or so.
2. **Trot in a straight line and time your walk with a stopwatch.** I built a 100m race track and determined that it consistently takes 26 seconds to complete the track in troll armor while carrying the hammer. This is 3.8 m/s. I can use this and a stopwatch to calculate distances fairly easily and accurately.

![Racetrack](http://kleemann.org/valheim/valmap-readme/racetrack.png)

3. **Estimate.** Just look into the distance and guess. This is the least accurate method but sometimes (like looking over a canyon) there is no alternative.

I ended up using (2) the most. Starting from the stones, I can trot in an orthogonal direction and time the distance. To reach a non-orthogonal position I can can combine two orthogonal positions. E.g., if I want to reach a spot in the North West, I can first time a path north, then a path west.

### Mapping

All these coordinates and times can get tricky so it's useful to have a spreadsheet to keep track of it all. The spreadsheet has a function ```trott2m(min, sec)=(60*min+sec)*3.8``` which converts troll armor trotting speed to meters. This lets you do something like:

| | A | B | C | D |
| -- | ------- | -------------------- | ---------- | ------- |
| 1 | **Glyph** | **Description** | **X** | **Y** |
| 2 | Stones | Starting spot | `0` | `0` |
| 3 | Compass | West of start | `=C2-trott2m(0,35)` | `=D2+5` |
| 4 | LineRight | Northern coast | `=C3` | `=D3+trott2m(1,12)` |

* The stones are at the origin.
* The compass is a 35 second trot west of the stones and five meters north.
* The northern coast is a 1 minute and 12 second trot directly north of the compass.

Note: these units are positive meters east and north and negative meters west and south.

This creates a daisy chain where each location depends on the location of a previous location. If you ever need to re-measure one of the old locations it will automatically update all subsequent locations.

My first version of the map was a hand translation of this spreadsheet to graph paper. I used the spreadheet to convert meters to graph paper units but the results were cumbersome, error prone, and not resiliant to map changes. Here's an early version of the starting continent: Stonia.

![graph paper version of Stonia](http://kleemann.org/valheim/valmap-readme/stonia-graph.png)

Since I already had the map data in a spreadsheet, I figured a program would make all this easier. This scala program reads the CSV exported by the spreadsheet and renders all the glyphs in a PDF. Some of the features include:

* chooses portrait or landscape for the best use of space
* scales the map within the page
* centers the map on the page
* provides efficient margins
* provides location specific glyphs
  * ***Stones***: ![Glyph Stones](http://kleemann.org/valheim/valmap-readme/glyph-stones.png) the starting stones location
  * ***Boss***: ![Glyph Boss](http://kleemann.org/valheim/valmap-readme/glyph-boss.png) a Forsaken shrine
  * ***Compass***: ![Glyph Compass](http://kleemann.org/valheim/valmap-readme/glyph-compass.png) an obelisk with a board facing north
  * ***Shack***: ![Glyph Shack](http://kleemann.org/valheim/valmap-readme/glyph-shack.png) a fire, bed, repair stations, chests, and some comfort. There are many of these on a continent.
  * ***Base***: ![Glyph Base](http://kleemann.org/valheim/valmap-readme/glyph-base.png) like a shack but with farms, smelters, tamed animals, everything you would expect on a full base. There is usually at most one of these on a continent.
  * ***LineUp, LineRight, LineDiag1, LineDiag2***: ![Glyph Line](http://kleemann.org/valheim/valmap-readme/glyph-line.png) a line in one of the eight cardinal and intercardinal directions. This is useful to emphasize coastlines and biome borders.
  * ***Road***: ![Glyph Road](http://kleemann.org/valheim/valmap-readme/glyph-road.png) a horizontal or vertical dashed line that represents the main road.
  * ***Circle***: a light gray circle that can represent mountain peaks, villages, or other points of interest.
* light gray orthogonal grid lines are drawn every 100 meters and dark gray grid lines are drawn every kilometer.
* an optional bright rectangle is drawn around the extent of the map. This is useful in draft versions but usually turned off for the final version.

The generated map is very sparse but is useful to print out and doodle on when exploring. When the continent is fully explored and all points of interest have been noted I print the final version and add details with a fountain pen and colored pencils.

Stonia (the starting continent) draft and final version:

![Stonia draft](http://kleemann.org/valheim/valmap-readme/stonia-draft.png)

![Stonia final](http://kleemann.org/valheim/valmap-readme/stonia-final.png)

There is a high level map that represents the continents as abstract shapes. It is not supposed to represent the actual shapes and sizes but is instead meant to facilitate sailing. It takes approximately one minute to sail a karve between Stonia/Trollfall and Turvy/Land Ho.

![Topsy Turvy final](http://kleemann.org/valheim/valmap-readme/topmap-cropped.svg)

Topsy Turvy (the continent due west of Stonia) draft and final version:

![Topsy Turvy draft](http://kleemann.org/valheim/valmap-readme/topsyturvy-draft.png)

![Topsy Turvy final](http://kleemann.org/valheim/valmap-readme/topsyturvy-final.png)

Lump (the continent due north of Stonia) draft and final version:

![Lump draft](http://kleemann.org/valheim/valmap-readme/lump-draft.png)

![Lump final](http://kleemann.org/valheim/valmap-readme/lump-final.png)

South Bounty (the continent dur north of Lump and Topsy) draft and final version:

![South Bounty draft](http://kleemann.org/valheim/valmap-readme/southbounty-draft.png)

![South Bounty final](http://kleemann.org/valheim/valmap-readme/southbounty-final.png)

Note: the east/west road in Turvy is at the same lattitude as the east/west road in Stonia. Similarly, the north/south road in Lump and South Bounty is at the same longitude as the north/south road in Stonia.
