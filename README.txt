 - March 9 2017

Added access rule ; accessible for 'javafx/**'
(See project properties > Java Build Path > Libraries )

This is necessary in pure-eclipse-only developing environment
  for checking stability of target operating system. 


 - May 12 2017

Planned client-server connection to basically separated 2 sockets.
One is for non-continuous(discrete) data. e.g.) control signal, non-audio data.
Another is for continuous audio data only.

File data also may be continuous, but this can't be taken for infinite time.
So it seems to be reasonable that a connection may be open additionally when someone wants send a file to other.
(But it's not considered to implement the function sending a file)