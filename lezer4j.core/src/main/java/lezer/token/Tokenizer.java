package lezer.token;

import lezer.tree.Input;

public interface Tokenizer {

	 void token(Input input, Token token, Stack stack);
	 boolean contextual();
	 boolean fallback();
	 boolean extend();
	  
}
