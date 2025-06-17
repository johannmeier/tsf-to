# tsf-to

## Note lines
* Tonic-SolFa note signs eg. ":.,"
  * ! or | for bar
  * ; for accented note
  * Triplets are separated with /
  * Eighth are separated with a backtick (:d `d ,d `d .d :)
  * key change: -
* _<float> underline number of notes
* =<float> underpoint number of notes
* two notes in one column: :d+ r (needed for formatting)
* ,, is ., in  column  (HQQ - HH) (needed for formatting)
* double note: d* (needed for formatting)
* Question marks are ignored

## Symbol line

* A line with symbols starts with s:
* If a ~ is contained in a TextLine it will be treated as a symbol line.
* Following symbols are recognized:

| sign | meaning |
|------|-------- |
| ^    | fermata sign |
| %    | segno sign |
| $    | coda sign |
| DS   | dal segno |
| DC   | da capo |
| 1.   | first repeat |
| 2.   | second repeat |
| f    | forte |
| mf   | mezzo forte |
| ff   | forte fortissimum |
| p    | piano |
| pp   | piano pianissimum |
| ppp  | ppp |
| \>   | filler (right>left*7 box 7 columns 'right' right aligned and 'left' left aligned) |
| _    | space |
| *    | empty column |
| b:int | Bar number |
| p:int | Song Part, e.g. A,B,C |

## Text lines

| sign | meaning |
|----- |-------- |
| *text | right aligned to column |
| text* | left aligned to column |
| * | empty column |
| ! | measure |
| !! | double measure |
| \> | filler  (>text --> left aligned to column) |
| _ | Space |
| 1. 2. ... | numbering of verses (column added) |


Add verse numbers to text lines:

    * :d
    1. hello
    2. hello


## new idea

* Tabular only with equal size columns 
* Placement of half, quarter, third, etc. ist made in the cell
* Text and symbol lines 
  * Position with tonic-solfa signs 
    * '.' -> Text starts at the middle of the column width
    * '.,' -> Text starts at the third quarter of the column width
  * Each word start left of the cell and is one column long
  * '_' is a blank
  * '*' is a filler for one column
  * '*Hello' is Hello right aligned (in total 2 columns) 
  * 'Hello**' is Hello left aligned (in total 3 columns)
  * '.Hello**' is Hello left aligned, starting at the half of the first column (in total 3 columns)
  * '.*Hello' is Hello left aligned, starting at the half of the first column (in total 3 columns)

