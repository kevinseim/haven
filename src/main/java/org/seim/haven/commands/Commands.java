package org.seim.haven.commands;

import org.seim.haven.commands.blobs.Decr;
import org.seim.haven.commands.blobs.DecrBy;
import org.seim.haven.commands.blobs.Get;
import org.seim.haven.commands.blobs.Incr;
import org.seim.haven.commands.blobs.IncrBy;
import org.seim.haven.commands.blobs.Mget;
import org.seim.haven.commands.blobs.Set;
import org.seim.haven.commands.blobs.Strlen;
import org.seim.haven.commands.keys.Del;
import org.seim.haven.commands.keys.Expire;
import org.seim.haven.commands.keys.ExpireAt;
import org.seim.haven.commands.keys.Persist;
import org.seim.haven.commands.keys.TOE;
import org.seim.haven.commands.keys.Type;
import org.seim.haven.commands.server.Flushall;
import org.seim.haven.commands.server.Ping;
import org.seim.haven.commands.transactions.Discard;
import org.seim.haven.commands.transactions.Exec;
import org.seim.haven.commands.transactions.Multi;

/**
 * Command singleton reference.
 * @author Kevin Seim
 */
public final class Commands {

  // server commands
  public static final Command PING = new Ping();
  public static final Command FLUSHALL = new Flushall();
  
  // key commands
  public static final Command DEL = new Del();
  public static final Command EXPIRE = new Expire();
  public static final Command EXPIREAT = new ExpireAt();
  public static final Command PERSIST = new Persist();
  public static final Command TOE = new TOE();
  public static final Command TYPE = new Type();
  
  // string commands
  public static final Command DECR = new Decr();
  public static final Command DECRBY = new DecrBy();
  public static final Command GET = new Get();
  public static final Command INCR = new Incr();
  public static final Command INCRBY = new IncrBy();
  public static final Command MGET = new Mget();
  public static final Command SET = new Set();
  public static final Command STRLEN = new Strlen();
  
  // transaction commands
  public static final Command DISCARD = new Discard();
  public static final Command EXEC = new Exec();
  public static final Command MULTI = new Multi();
  
  // list of all commands
  public static final Command[] list = new Command[] {
    PING,
    FLUSHALL,
    
    DEL,
    EXPIRE,
    EXPIREAT,
    PERSIST,
    TOE,
    TYPE,
    
    DECR,
    DECRBY,
    GET,
    INCR,
    INCRBY,
    MGET,
    SET,
    STRLEN,
    
    DISCARD,
    EXEC,
    MULTI
  };
  
  private Commands() { }
}
