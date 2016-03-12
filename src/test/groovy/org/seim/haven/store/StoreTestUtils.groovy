package org.seim.haven.store

import org.seim.haven.util.Charsets


class StoreTestUtils {

  static byte[] toCommitLogMessage(String... args) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(256)
    for (String arg : args) {
      out.write(arg.getBytes(Charsets.UTF8))
      out.write((byte) '\r')
      out.write((byte) '\n')
    }
    return out.toByteArray()
  }
  
  static InputStream toCommitLogStream(String... args) {
    return new ByteArrayInputStream(toCommitLogMessage(args))
  }

}
