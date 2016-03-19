# Haven
The beginnings of a Redis like server for other data structures (e.g. Count Min Sketch).  The code is
currently only in a "proof of concept" state.

#### Protocol
Haven uses the Redis wire protocol documented [here](http://redis.io/topics/protocol).  The default port is 7072.

#### Persistence
Haven uses a commit log and periodic snapshots to persist its in memory data structures to disk.  By default, 
the commit log is flushed to disk every 1 second.  Commit log segments roll when the file size exceeds 4mb.
By default, snapshots are taken every 30 seconds if 1000 or more transactions have been logged.  When a snapshot
is taken, older commit log segments are deleted if no longer needed.  On startup, the snapshot is loaded and
the commit log is replayed.

#### Clustering
(Not supported)