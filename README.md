# tsf-to

## Note lines
* Tonic-SolFa note signs eg. ":.,"
  * ! or | for bar
  * ; for accented note
  * Triplets are separated with /
  * Eighth are separated with a backtick (:d `d ,d `d .d :)
  * ,, is ., in  column  (HQQ - HH) (needed for formatting)
  * text: "
  * after note: 
    * key change: -
    * stack notes: %
    * tenuto: ~
    * staccato: .
    * portato: tenuto + staccato ~.
    * accented: >
    * marcato: ^
    * _<float> underline number of notes
    * =<float> underpoint number of notes
    * two notes in one column: :d+ r (needed for formatting)
    * double note: d* (needed for formatting)
* Question marks are ignored

## Symbol line

* A line with symbols starts with s:
* If a ~ is contained in a TextLine it will be treated as a symbol line.
* Following symbols are recognized:

| sign      | meaning                                                                           |
|-----------|-----------------------------------------------------------------------------------|
| ^         | fermata sign                                                                      |
| %         | segno sign                                                                        |
| $         | coda sign                                                                         |
| ds        | dal segno                                                                         |
| ds        | da capo                                                                           |
| 1.        | first repeat                                                                      |
| 2.        | second repeat                                                                     |
| f         | forte                                                                             |
| mf        | mezzo forte                                                                       |
| ff        | forte fortissimum                                                                 |
| p         | piano                                                                             |
| pp        | piano pianissimum                                                                 |
| ppp       | ppp                                                                               |
| \>        | filler (right>left*7 box 7 columns 'right' right aligned and 'left' left aligned) |
| _         | space                                                                             |
| *         | empty column                                                                      |
| b:int     | Bar number                                                                        |
| p:int     | Song Part, e.g. A,B,C                                                             |
| bpm:int   | Pulse                                                                             |

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
