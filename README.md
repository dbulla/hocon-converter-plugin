# hocon-converter-plugin
Plugin for Intellij IDEA which converts HOCON back and forth between map-like and property-like formats.  This can be really useful when you need 
to change Spring property keys (which are resolved like 
```
"${aaa.bbb.ccc.ddd}"
```
and your files are in .conf format, like 
```
aaa {
  bbb {
    ccc {
      ddd = "some value"
    }
  }
}
```
Simply convert the .conf-style file into property-style, then you can search/replace

### Features completed
 - read in basic properties-formatted file with simple key/value pairs
 - read in basic conf-formatted file wiht simple key/value pairs & maps
 - Keep "include" at top of file (in order)
 - Top-level lists (i.e.,
 ```
 cors = [
   "xxxx",
   "yyyy"
 ]
 ```
### What's NOT working
- Comments
- Lists inside maps or other lists (only top-level lists are working currently)



### Planned features
 - Tie comments to line that follows (currently comments are lost)
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
 - Auto-detection of if the text is from a .yml file - if so, then it needs to be indented after the  `app |-` line
 - If possible, do search/replace in-place, even when the file is in .conf format

