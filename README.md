# hocon-converter-plugin
Plugin for Intellij IDEA which converts HOCON back and forth between map-like and property-like formats

### Features completed
 - read in basic properties-formatted file with simple key/value pairs
 - read in basic conf-formatted file wiht simple key/value pairs & maps
 - Keep "include" at top of file (in order)


### Planned features
 - Tie comments to line that follows (currently comments are lost)
 - Lists (i.e.,
 ```
 cors = [
 "xxxx",
 "yyyy"
 ]
 ```
 - If a map has only a single key/value, then don't display it in map format in conf output, use key/value output.  For example:
 ```
 aaa.bbb.ccc = 5
 ```
instead of
```
aaa {
  bbb {
    ccc = 5
  }
}
```