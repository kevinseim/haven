package org.seim.haven.commands.impl

import org.seim.haven.models.Token;
import org.seim.haven.response.Response
import org.seim.haven.store.ReplayState;

class TestFlexCommand extends FlexCommand {
  
  FlexRequest parse(String line) {
    String[] args = line.split(" ");
    Token[] tokens = new Token[args.length];
    for (int i=0; i<args.length; i++) {
      tokens[i] = new Token(args[i]);
    }
    return parse(tokens);
  }
  
  @Override
  protected Response process(FlexRequest request) {
    return null;
  }

  @Override
  protected boolean replay(ReplayState state, FlexRequest request) {
    return false;
  }
}
