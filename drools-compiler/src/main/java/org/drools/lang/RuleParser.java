// $ANTLR 3.0ea8 D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g 2007-03-28 19:44:10

	package org.drools.lang;
	import java.util.List;
	import java.util.ArrayList;
	import java.util.Iterator;
	import java.util.StringTokenizer;
	import org.drools.lang.descr.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class RuleParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "EOL", "ID", "INT", "BOOL", "STRING", "FLOAT", "MISC", "WS", "EscapeSequence", "HexDigit", "UnicodeEscape", "OctalEscape", "SH_STYLE_SINGLE_LINE_COMMENT", "C_STYLE_SINGLE_LINE_COMMENT", "MULTI_LINE_COMMENT", "\';\'", "\'package\'", "\'import\'", "\'.\'", "\'.*\'", "\'expander\'", "\'global\'", "\'function\'", "\'(\'", "\',\'", "\')\'", "\'{\'", "\'}\'", "\'query\'", "\'end\'", "\'rule\'", "\'when\'", "\':\'", "\'then\'", "\'attributes\'", "\'salience\'", "\'no-loop\'", "\'auto-focus\'", "\'activation-group\'", "\'agenda-group\'", "\'duration\'", "\'or\'", "\'||\'", "\'==\'", "\'>\'", "\'>=\'", "\'<\'", "\'<=\'", "\'!=\'", "\'contains\'", "\'matches\'", "\'excludes\'", "\'null\'", "\'->\'", "\'and\'", "\'&&\'", "\'exists\'", "\'not\'", "\'eval\'", "\'[\'", "\']\'", "\'use\'"
    };
    public static final int UnicodeEscape=14;
    public static final int BOOL=7;
    public static final int HexDigit=13;
    public static final int EscapeSequence=12;
    public static final int INT=6;
    public static final int WS=11;
    public static final int EOF=-1;
    public static final int MISC=10;
    public static final int STRING=8;
    public static final int EOL=4;
    public static final int FLOAT=9;
    public static final int SH_STYLE_SINGLE_LINE_COMMENT=16;
    public static final int OctalEscape=15;
    public static final int MULTI_LINE_COMMENT=18;
    public static final int C_STYLE_SINGLE_LINE_COMMENT=17;
    public static final int ID=5;
        public RuleParser(TokenStream input) {
            super(input);
        }
        

    public String[] getTokenNames() { return tokenNames; }

    
    	private ExpanderResolver expanderResolver;
    	private Expander expander;
    	private boolean expanderDebug = false;
    	private PackageDescr packageDescr;
    	private List errors = new ArrayList();
    	private String source = "unknown";
    	private int lineOffset = 0;
    	
    	private boolean parserDebug = false;
    	
    	public void setParserDebug(boolean parserDebug) {
    		this.parserDebug = parserDebug;
    	}
    	
    	public void debug(String message) {
    		if ( parserDebug ) 
    			System.err.println( "drl parser: " + message );
    	}
    	
    	public void setSource(String source) {
    		this.source = source;
    	}
    
    	/**
    	 * This may be set to enable debuggin of DSLs/expanders.
    	 * If set to true, expander stuff will be sent to the Std out.
    	 */	
    	public void setExpanderDebug(boolean status) {
    		expanderDebug = status;
    	}
    	public String getSource() {
    		return this.source;
    	}
    	
    	public PackageDescr getPackageDescr() {
    		return packageDescr;
    	}
    	
    	private int offset(int line) {
    		return line + lineOffset;
    	}
    	
    	/**
    	 * This will set the offset to record when reparsing. Normally is zero of course 
    	 */
    	public void setLineOffset(int i) {
    	 	this.lineOffset = i;
    	}
    	
    	public void setExpanderResolver(ExpanderResolver expanderResolver) {
    		this.expanderResolver = expanderResolver;
    	}
    	
    	public ExpanderResolver getExpanderResolver() {
    		return expanderResolver;
    	}
    	
    	/** Expand the LHS */
    	private String runWhenExpander(String text, int line) throws RecognitionException {
    		String expanded = text.trim();
    		if (expanded.startsWith(">")) {
    			expanded = expanded.substring(1);  //escape !!
    		} else {
    			try {
    				expanded = expander.expand( "when", text );			
    			} catch (Exception e) {
    				this.errors.add(new ExpanderException("Unable to expand: " + text + ". Due to " + e.getMessage(), line));
    				return "";
    			}
    		}
    		if (expanderDebug) {
    			System.out.println("Expanding LHS: " + text + " ----> " + expanded + " --> from line: " + line);
    		}
    		return expanded;	
    		
    	}
    	
        	/** This will apply a list of constraints to an LHS block */
        	private String applyConstraints(List constraints, String block) {
        		//apply the constraints as a comma seperated list inside the previous block
        		//the block will end in something like "foo()" and the constraint patterns will be put in the ()
        		if (constraints == null) {
        			return block;
        		}
        		StringBuffer list = new StringBuffer();    		
        		for (Iterator iter = constraints.iterator(); iter.hasNext();) {
    				String con = (String) iter.next();
    				list.append("\n\t\t");
    				list.append(con);
    				if (iter.hasNext()) {
    					list.append(",");					
    				}			
    			}
        		if (block.endsWith("()")) {
        			return block.substring(0, block.length() - 2) + "(" + list.toString() + ")";
        		} else {
        			return block + "(" + list.toString() + ")";
        		}
        	}  	
    
            /** Reparse the results of the expansion */
        	private void reparseLhs(String text, AndDescr descrs) throws RecognitionException {
        		CharStream charStream = new ANTLRStringStream( text );
        		RuleParserLexer lexer = new RuleParserLexer( charStream );
        		TokenStream tokenStream = new CommonTokenStream( lexer );
        		RuleParser parser = new RuleParser( tokenStream );
        		parser.setLineOffset( descrs.getLine() );
        		parser.normal_lhs_block(descrs);
                
                    if (parser.hasErrors()) {
        			this.errors.addAll(parser.getErrors());
        		}
    		if (expanderDebug) {
    			System.out.println("Reparsing LHS: " + text + " --> successful:" + !parser.hasErrors());
    		}    		
        		
        	}
    	
    	/** Expand a line on the RHS */
    	private String runThenExpander(String text, int startLine) {
    		//System.err.println( "expand THEN [" + text + "]" );
    		StringTokenizer lines = new StringTokenizer( text, "\n\r" );
    
    		StringBuffer expanded = new StringBuffer();
    		
    		String eol = System.getProperty( "line.separator" );
    				
    		while ( lines.hasMoreTokens() ) {
    			startLine++;
    			String line = lines.nextToken();
    			line = line.trim();
    			if ( line.length() > 0 ) {
    				if ( line.startsWith( ">" ) ) {
    					expanded.append( line.substring( 1 ) );
    					expanded.append( eol );
    				} else {
    					try {
    						expanded.append( expander.expand( "then", line ) );
    						expanded.append( eol );
    					} catch (Exception e) {
    						this.errors.add(new ExpanderException("Unable to expand: " + line + ". Due to " + e.getMessage(), startLine));			
    					}
    				}
    			}
    		}
    		
    		if (expanderDebug) {
    			System.out.println("Expanding RHS: " + text + " ----> " + expanded.toString() + " --> from line starting: " + startLine);
    		}		
    		
    		return expanded.toString();
    	}
    	
    
    	
    	private String getString(Token token) {
    		String orig = token.getText();
    		return orig.substring( 1, orig.length() -1 );
    	}
    	
    	public void reportError(RecognitionException ex) {
    	        // if we've already reported an error and have not matched a token
                    // yet successfully, don't report any errors.
                    if ( errorRecovery ) {
                            return;
                    }
                    errorRecovery = true;
    
    		ex.line = offset(ex.line); //add the offset if there is one
    		errors.add( ex ); 
    	}
         	
         	/** return the raw RecognitionException errors */
         	public List getErrors() {
         		return errors;
         	}
         	
         	/** Return a list of pretty strings summarising the errors */
         	public List getErrorMessages() {
         		List messages = new ArrayList();
     		for ( Iterator errorIter = errors.iterator() ; errorIter.hasNext() ; ) {
         	     		messages.add( createErrorMessage( (RecognitionException) errorIter.next() ) );
         	     	}
         	     	return messages;
         	}
         	
         	/** return true if any parser errors were accumulated */
         	public boolean hasErrors() {
      		return ! errors.isEmpty();
         	}
         	
         	/** This will take a RecognitionException, and create a sensible error message out of it */
         	public String createErrorMessage(RecognitionException e)
            {
    		StringBuffer message = new StringBuffer();		
                    message.append( source + ":"+e.line+":"+e.charPositionInLine+" ");
                    if ( e instanceof MismatchedTokenException ) {
                            MismatchedTokenException mte = (MismatchedTokenException)e;
                            message.append("mismatched token: "+
                                                               e.token+
                                                               "; expecting type "+
                                                               tokenNames[mte.expecting]);
                    }
                    else if ( e instanceof MismatchedTreeNodeException ) {
                            MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
                            message.append("mismatched tree node: "+
                                                               mtne.foundNode+
                                                               "; expecting type "+
                                                               tokenNames[mtne.expecting]);
                    }
                    else if ( e instanceof NoViableAltException ) {
                            NoViableAltException nvae = (NoViableAltException)e;
    			message.append( "Unexpected token '" + e.token.getText() + "'" );
                            /*
                            message.append("decision=<<"+nvae.grammarDecisionDescription+">>"+
                                                               " state "+nvae.stateNumber+
                                                               " (decision="+nvae.decisionNumber+
                                                               ") no viable alt; token="+
                                                               e.token);
                                                               */
                    }
                    else if ( e instanceof EarlyExitException ) {
                            EarlyExitException eee = (EarlyExitException)e;
                            message.append("required (...)+ loop (decision="+
                                                               eee.decisionNumber+
                                                               ") did not match anything; token="+
                                                               e.token);
                    }
                    else if ( e instanceof MismatchedSetException ) {
                            MismatchedSetException mse = (MismatchedSetException)e;
                            message.append("mismatched token '"+
                                                               e.token+
                                                               "' expecting set "+mse.expecting);
                    }
                    else if ( e instanceof MismatchedNotSetException ) {
                            MismatchedNotSetException mse = (MismatchedNotSetException)e;
                            message.append("mismatched token '"+
                                                               e.token+
                                                               "' expecting set "+mse.expecting);
                    }
                    else if ( e instanceof FailedPredicateException ) {
                            FailedPredicateException fpe = (FailedPredicateException)e;
                            message.append("rule "+fpe.ruleName+" failed predicate: {"+
                                                               fpe.predicateText+"}?");
                    } else if (e instanceof GeneralParseException) {
    			message.append(" " + e.getMessage());
    		}
                   	return message.toString();
            }   
            
            void checkTrailingSemicolon(String text, int line) {
            	if (text.trim().endsWith( ";" ) ) {
            		this.errors.add( new GeneralParseException( "Trailing semi-colon not allowed", offset(line) ) );
            	}
            }
          



    // $ANTLR start opt_eol
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:275:1: opt_eol : ( (';'|EOL))* ;
    public void opt_eol() throws RecognitionException {   
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:276:17: ( ( (';'|EOL))* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:276:17: ( (';'|EOL))*
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:276:17: ( (';'|EOL))*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);
                if ( LA1_0==EOL ) {
                    alt1=1;
                }
                else if ( LA1_0==19 ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:276:18: (';'|EOL)
            	    {
            	    if ( input.LA(1)==EOL||input.LA(1)==19 ) {
            	        input.consume();
            	        errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recoverFromMismatchedSet(input,mse,FOLLOW_set_in_opt_eol41);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end opt_eol


    // $ANTLR start compilation_unit
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:279:1: compilation_unit : opt_eol prolog (r= rule | q= query | extra_statement )* ;
    public void compilation_unit() throws RecognitionException {   
        RuleDescr r = null;

        QueryDescr q = null;


        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:280:17: ( opt_eol prolog (r= rule | q= query | extra_statement )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:280:17: opt_eol prolog (r= rule | q= query | extra_statement )*
            {
            following.push(FOLLOW_opt_eol_in_compilation_unit57);
            opt_eol();
            following.pop();

            following.push(FOLLOW_prolog_in_compilation_unit61);
            prolog();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:282:17: (r= rule | q= query | extra_statement )*
            loop2:
            do {
                int alt2=4;
                alt2 = dfa2.predict(input); 
                switch (alt2) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:282:25: r= rule
            	    {
            	    following.push(FOLLOW_rule_in_compilation_unit70);
            	    r=rule();
            	    following.pop();

            	    this.packageDescr.addRule( r ); 

            	    }
            	    break;
            	case 2 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:283:25: q= query
            	    {
            	    following.push(FOLLOW_query_in_compilation_unit83);
            	    q=query();
            	    following.pop();

            	    this.packageDescr.addRule( q ); 

            	    }
            	    break;
            	case 3 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:284:25: extra_statement
            	    {
            	    following.push(FOLLOW_extra_statement_in_compilation_unit91);
            	    extra_statement();
            	    following.pop();


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end compilation_unit


    // $ANTLR start prolog
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:288:1: prolog : opt_eol (name= package_statement )? ( extra_statement | expander )* opt_eol ;
    public void prolog() throws RecognitionException {   
        String name = null;


        
        		String packageName = "";
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:292:17: ( opt_eol (name= package_statement )? ( extra_statement | expander )* opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:292:17: opt_eol (name= package_statement )? ( extra_statement | expander )* opt_eol
            {
            following.push(FOLLOW_opt_eol_in_prolog115);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:293:17: (name= package_statement )?
            int alt3=2;
            int LA3_0 = input.LA(1);
            if ( LA3_0==20 ) {
                alt3=1;
            }
            else if ( LA3_0==-1||LA3_0==EOL||LA3_0==19||LA3_0==21||(LA3_0>=24 && LA3_0<=26)||LA3_0==32||LA3_0==34 ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("293:17: (name= package_statement )?", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:293:19: name= package_statement
                    {
                    following.push(FOLLOW_package_statement_in_prolog123);
                    name=package_statement();
                    following.pop();

                     packageName = name; 

                    }
                    break;

            }

             
            			this.packageDescr = new PackageDescr( name ); 
            		
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:297:17: ( extra_statement | expander )*
            loop4:
            do {
                int alt4=3;
                alt4 = dfa4.predict(input); 
                switch (alt4) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:297:25: extra_statement
            	    {
            	    following.push(FOLLOW_extra_statement_in_prolog138);
            	    extra_statement();
            	    following.pop();


            	    }
            	    break;
            	case 2 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:298:25: expander
            	    {
            	    following.push(FOLLOW_expander_in_prolog144);
            	    expander();
            	    following.pop();


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            following.push(FOLLOW_opt_eol_in_prolog156);
            opt_eol();
            following.pop();


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end prolog


    // $ANTLR start package_statement
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:304:1: package_statement returns [String packageName] : 'package' opt_eol name= dotted_name ( ';' )? opt_eol ;
    public String package_statement() throws RecognitionException {   
        String packageName;
        String name = null;


        
        		packageName = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:309:17: ( 'package' opt_eol name= dotted_name ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:309:17: 'package' opt_eol name= dotted_name ( ';' )? opt_eol
            {
            match(input,20,FOLLOW_20_in_package_statement180); 
            following.push(FOLLOW_opt_eol_in_package_statement182);
            opt_eol();
            following.pop();

            following.push(FOLLOW_dotted_name_in_package_statement186);
            name=dotted_name();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:309:52: ( ';' )?
            int alt5=2;
            int LA5_0 = input.LA(1);
            if ( LA5_0==19 ) {
                alt5=1;
            }
            else if ( LA5_0==-1||LA5_0==EOL||LA5_0==21||(LA5_0>=24 && LA5_0<=26)||LA5_0==32||LA5_0==34 ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("309:52: ( \';\' )?", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:309:52: ';'
                    {
                    match(input,19,FOLLOW_19_in_package_statement188); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_package_statement191);
            opt_eol();
            following.pop();

            
            			packageName = name;
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return packageName;
    }
    // $ANTLR end package_statement


    // $ANTLR start import_statement
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:315:1: import_statement : 'import' opt_eol name= import_name ( ';' )? opt_eol ;
    public void import_statement() throws RecognitionException {   
        String name = null;


        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:316:17: ( 'import' opt_eol name= import_name ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:316:17: 'import' opt_eol name= import_name ( ';' )? opt_eol
            {
            match(input,21,FOLLOW_21_in_import_statement207); 
            following.push(FOLLOW_opt_eol_in_import_statement209);
            opt_eol();
            following.pop();

            following.push(FOLLOW_import_name_in_import_statement213);
            name=import_name();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:316:51: ( ';' )?
            int alt6=2;
            int LA6_0 = input.LA(1);
            if ( LA6_0==19 ) {
                alt6=1;
            }
            else if ( LA6_0==-1||LA6_0==EOL||LA6_0==21||(LA6_0>=24 && LA6_0<=26)||LA6_0==32||LA6_0==34 ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("316:51: ( \';\' )?", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:316:51: ';'
                    {
                    match(input,19,FOLLOW_19_in_import_statement215); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_import_statement218);
            opt_eol();
            following.pop();

            
            			if (packageDescr != null) 
            				packageDescr.addImport( name );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end import_statement


    // $ANTLR start import_name
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:323:1: import_name returns [String name] : id= ID ( '.' id= ID )* (star= '.*' )? ;
    public String import_name() throws RecognitionException {   
        String name;
        Token id=null;
        Token star=null;

        
        		name = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:328:17: (id= ID ( '.' id= ID )* (star= '.*' )? )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:328:17: id= ID ( '.' id= ID )* (star= '.*' )?
            {
            id=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_import_name249); 
             name=id.getText(); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:328:46: ( '.' id= ID )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);
                if ( LA7_0==22 ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:328:48: '.' id= ID
            	    {
            	    match(input,22,FOLLOW_22_in_import_name255); 
            	    id=(Token)input.LT(1);
            	    match(input,ID,FOLLOW_ID_in_import_name259); 
            	     name = name + "." + id.getText(); 

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:328:99: (star= '.*' )?
            int alt8=2;
            int LA8_0 = input.LA(1);
            if ( LA8_0==23 ) {
                alt8=1;
            }
            else if ( LA8_0==-1||LA8_0==EOL||LA8_0==19||LA8_0==21||(LA8_0>=24 && LA8_0<=26)||LA8_0==32||LA8_0==34 ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("328:99: (star= \'.*\' )?", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:328:100: star= '.*'
                    {
                    star=(Token)input.LT(1);
                    match(input,23,FOLLOW_23_in_import_name269); 
                     name = name + star.getText(); 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return name;
    }
    // $ANTLR end import_name


    // $ANTLR start expander
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:330:1: expander : 'expander' (name= dotted_name )? ( ';' )? opt_eol ;
    public void expander() throws RecognitionException {   
        String name = null;


        
        		String config=null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:334:17: ( 'expander' (name= dotted_name )? ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:334:17: 'expander' (name= dotted_name )? ( ';' )? opt_eol
            {
            match(input,24,FOLLOW_24_in_expander289); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:334:28: (name= dotted_name )?
            int alt9=2;
            int LA9_0 = input.LA(1);
            if ( LA9_0==ID ) {
                alt9=1;
            }
            else if ( LA9_0==-1||LA9_0==EOL||LA9_0==19||LA9_0==21||(LA9_0>=24 && LA9_0<=26)||LA9_0==32||LA9_0==34 ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("334:28: (name= dotted_name )?", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:334:29: name= dotted_name
                    {
                    following.push(FOLLOW_dotted_name_in_expander294);
                    name=dotted_name();
                    following.pop();


                    }
                    break;

            }

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:334:48: ( ';' )?
            int alt10=2;
            int LA10_0 = input.LA(1);
            if ( LA10_0==19 ) {
                alt10=1;
            }
            else if ( LA10_0==-1||LA10_0==EOL||LA10_0==21||(LA10_0>=24 && LA10_0<=26)||LA10_0==32||LA10_0==34 ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("334:48: ( \';\' )?", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:334:48: ';'
                    {
                    match(input,19,FOLLOW_19_in_expander298); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_expander301);
            opt_eol();
            following.pop();

            
            			if (expanderResolver == null) 
            				throw new IllegalArgumentException("Unable to use expander. Make sure a expander or dsl config is being passed to the parser. [ExpanderResolver was not set].");
            			if ( expander != null )
            				throw new IllegalArgumentException( "Only one 'expander' statement per file is allowed" );
            			expander = expanderResolver.get( name, config );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end expander


    // $ANTLR start global
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:344:1: global : 'global' type= dotted_name id= ID ( ';' )? opt_eol ;
    public void global() throws RecognitionException {   
        Token id=null;
        String type = null;


        
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:348:17: ( 'global' type= dotted_name id= ID ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:348:17: 'global' type= dotted_name id= ID ( ';' )? opt_eol
            {
            match(input,25,FOLLOW_25_in_global325); 
            following.push(FOLLOW_dotted_name_in_global329);
            type=dotted_name();
            following.pop();

            id=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_global333); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:348:49: ( ';' )?
            int alt11=2;
            int LA11_0 = input.LA(1);
            if ( LA11_0==19 ) {
                alt11=1;
            }
            else if ( LA11_0==-1||LA11_0==EOL||LA11_0==21||(LA11_0>=24 && LA11_0<=26)||LA11_0==32||LA11_0==34 ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("348:49: ( \';\' )?", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:348:49: ';'
                    {
                    match(input,19,FOLLOW_19_in_global335); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_global338);
            opt_eol();
            following.pop();

            
            			packageDescr.addGlobal( id.getText(), type );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end global


    // $ANTLR start function
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:354:1: function : 'function' opt_eol (retType= dotted_name )? opt_eol name= ID opt_eol '(' opt_eol ( (paramType= dotted_name )? opt_eol paramName= argument opt_eol ( ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )* )? ')' opt_eol '{' body= curly_chunk '}' opt_eol ;
    public void function() throws RecognitionException {   
        Token name=null;
        String retType = null;

        String paramType = null;

        String paramName = null;

        String body = null;


        
        		FunctionDescr f = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:359:17: ( 'function' opt_eol (retType= dotted_name )? opt_eol name= ID opt_eol '(' opt_eol ( (paramType= dotted_name )? opt_eol paramName= argument opt_eol ( ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )* )? ')' opt_eol '{' body= curly_chunk '}' opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:359:17: 'function' opt_eol (retType= dotted_name )? opt_eol name= ID opt_eol '(' opt_eol ( (paramType= dotted_name )? opt_eol paramName= argument opt_eol ( ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )* )? ')' opt_eol '{' body= curly_chunk '}' opt_eol
            {
            match(input,26,FOLLOW_26_in_function362); 
            following.push(FOLLOW_opt_eol_in_function364);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:359:36: (retType= dotted_name )?
            int alt12=2;
            alt12 = dfa12.predict(input); 
            switch (alt12) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:359:37: retType= dotted_name
                    {
                    following.push(FOLLOW_dotted_name_in_function369);
                    retType=dotted_name();
                    following.pop();


                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_function373);
            opt_eol();
            following.pop();

            name=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_function377); 
            following.push(FOLLOW_opt_eol_in_function379);
            opt_eol();
            following.pop();

            
            			//System.err.println( "function :: " + name.getText() );
            			f = new FunctionDescr( name.getText(), retType );
            		
            match(input,27,FOLLOW_27_in_function388); 
            following.push(FOLLOW_opt_eol_in_function390);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:365:25: ( (paramType= dotted_name )? opt_eol paramName= argument opt_eol ( ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )* )?
            int alt16=2;
            int LA16_0 = input.LA(1);
            if ( (LA16_0>=EOL && LA16_0<=ID)||LA16_0==19 ) {
                alt16=1;
            }
            else if ( LA16_0==29 ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("365:25: ( (paramType= dotted_name )? opt_eol paramName= argument opt_eol ( \',\' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )* )?", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:365:33: (paramType= dotted_name )? opt_eol paramName= argument opt_eol ( ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )*
                    {
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:365:33: (paramType= dotted_name )?
                    int alt13=2;
                    alt13 = dfa13.predict(input); 
                    switch (alt13) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:365:34: paramType= dotted_name
                            {
                            following.push(FOLLOW_dotted_name_in_function400);
                            paramType=dotted_name();
                            following.pop();


                            }
                            break;

                    }

                    following.push(FOLLOW_opt_eol_in_function404);
                    opt_eol();
                    following.pop();

                    following.push(FOLLOW_argument_in_function408);
                    paramName=argument();
                    following.pop();

                    following.push(FOLLOW_opt_eol_in_function410);
                    opt_eol();
                    following.pop();

                    
                    					f.addParameter( paramType, paramName );
                    				
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:369:33: ( ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);
                        if ( LA15_0==28 ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:369:41: ',' opt_eol (paramType= dotted_name )? opt_eol paramName= argument opt_eol
                    	    {
                    	    match(input,28,FOLLOW_28_in_function424); 
                    	    following.push(FOLLOW_opt_eol_in_function426);
                    	    opt_eol();
                    	    following.pop();

                    	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:369:53: (paramType= dotted_name )?
                    	    int alt14=2;
                    	    alt14 = dfa14.predict(input); 
                    	    switch (alt14) {
                    	        case 1 :
                    	            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:369:54: paramType= dotted_name
                    	            {
                    	            following.push(FOLLOW_dotted_name_in_function431);
                    	            paramType=dotted_name();
                    	            following.pop();


                    	            }
                    	            break;

                    	    }

                    	    following.push(FOLLOW_opt_eol_in_function435);
                    	    opt_eol();
                    	    following.pop();

                    	    following.push(FOLLOW_argument_in_function439);
                    	    paramName=argument();
                    	    following.pop();

                    	    following.push(FOLLOW_opt_eol_in_function441);
                    	    opt_eol();
                    	    following.pop();

                    	    
                    	    						f.addParameter( paramType, paramName );
                    	    					

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,29,FOLLOW_29_in_function466); 
            following.push(FOLLOW_opt_eol_in_function470);
            opt_eol();
            following.pop();

            match(input,30,FOLLOW_30_in_function474); 
            following.push(FOLLOW_curly_chunk_in_function481);
            body=curly_chunk();
            following.pop();

            
            				f.setText( body );
            			
            match(input,31,FOLLOW_31_in_function490); 
            
            			packageDescr.addFunction( f );
            		
            following.push(FOLLOW_opt_eol_in_function498);
            opt_eol();
            following.pop();


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end function


    // $ANTLR start query
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:390:1: query returns [QueryDescr query] : opt_eol loc= 'query' queryName= word opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) 'end' opt_eol ;
    public QueryDescr query() throws RecognitionException {   
        QueryDescr query;
        Token loc=null;
        String queryName = null;


        
        		query = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:395:17: ( opt_eol loc= 'query' queryName= word opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) 'end' opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:395:17: opt_eol loc= 'query' queryName= word opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) 'end' opt_eol
            {
            following.push(FOLLOW_opt_eol_in_query522);
            opt_eol();
            following.pop();

            loc=(Token)input.LT(1);
            match(input,32,FOLLOW_32_in_query528); 
            following.push(FOLLOW_word_in_query532);
            queryName=word();
            following.pop();

            following.push(FOLLOW_opt_eol_in_query534);
            opt_eol();
            following.pop();

             
            			query = new QueryDescr( queryName, null ); 
            			query.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            			AndDescr lhs = new AndDescr(); query.setLhs( lhs ); 
            			lhs.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )
            int alt17=2;
            switch ( input.LA(1) ) {
            case 27:
                int LA17_1 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 1, input);

                    throw nvae;
                }
                break;
            case EOL:
                int LA17_2 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 2, input);

                    throw nvae;
                }
                break;
            case 33:
                int LA17_3 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 3, input);

                    throw nvae;
                }
                break;
            case 60:
                int LA17_4 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 4, input);

                    throw nvae;
                }
                break;
            case 61:
                int LA17_5 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 5, input);

                    throw nvae;
                }
                break;
            case 62:
                int LA17_6 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 6, input);

                    throw nvae;
                }
                break;
            case ID:
                int LA17_7 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 7, input);

                    throw nvae;
                }
                break;
            case 19:
                int LA17_8 = input.LA(2);
                if (  expander != null  ) {
                    alt17=1;
                }
                else if ( true ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 8, input);

                    throw nvae;
                }
                break;
            case INT:
            case BOOL:
            case STRING:
            case FLOAT:
            case MISC:
            case WS:
            case EscapeSequence:
            case HexDigit:
            case UnicodeEscape:
            case OctalEscape:
            case SH_STYLE_SINGLE_LINE_COMMENT:
            case C_STYLE_SINGLE_LINE_COMMENT:
            case MULTI_LINE_COMMENT:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 63:
            case 64:
            case 65:
                alt17=1;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("403:17: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 17, 0, input);

                throw nvae;
            }

            switch (alt17) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:404:25: {...}? expander_lhs_block[lhs]
                    {
                    if ( !( expander != null ) ) {
                        throw new FailedPredicateException(input, "query", " expander != null ");
                    }
                    following.push(FOLLOW_expander_lhs_block_in_query550);
                    expander_lhs_block(lhs);
                    following.pop();


                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:405:27: normal_lhs_block[lhs]
                    {
                    following.push(FOLLOW_normal_lhs_block_in_query558);
                    normal_lhs_block(lhs);
                    following.pop();


                    }
                    break;

            }

            match(input,33,FOLLOW_33_in_query573); 
            following.push(FOLLOW_opt_eol_in_query575);
            opt_eol();
            following.pop();


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return query;
    }
    // $ANTLR end query


    // $ANTLR start rule
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:411:1: rule returns [RuleDescr rule] : opt_eol loc= 'rule' ruleName= word opt_eol ( rule_attributes[rule] )? opt_eol ( (loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )? ( opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )* )? )? 'end' opt_eol ;
    public RuleDescr rule() throws RecognitionException {   
        RuleDescr rule;
        Token loc=null;
        Token any=null;
        String ruleName = null;


        
        		rule = null;
        		String consequence = "";
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:417:17: ( opt_eol loc= 'rule' ruleName= word opt_eol ( rule_attributes[rule] )? opt_eol ( (loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )? ( opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )* )? )? 'end' opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:417:17: opt_eol loc= 'rule' ruleName= word opt_eol ( rule_attributes[rule] )? opt_eol ( (loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )? ( opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )* )? )? 'end' opt_eol
            {
            following.push(FOLLOW_opt_eol_in_rule598);
            opt_eol();
            following.pop();

            loc=(Token)input.LT(1);
            match(input,34,FOLLOW_34_in_rule604); 
            following.push(FOLLOW_word_in_rule608);
            ruleName=word();
            following.pop();

            following.push(FOLLOW_opt_eol_in_rule610);
            opt_eol();
            following.pop();

             
            			debug( "start rule: " + ruleName );
            			rule = new RuleDescr( ruleName, null ); 
            			rule.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:424:17: ( rule_attributes[rule] )?
            int alt18=2;
            switch ( input.LA(1) ) {
            case 36:
            case 38:
                alt18=1;
                break;
            case EOL:
            case 19:
            case 28:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
                alt18=1;
                break;
            case 35:
                alt18=1;
                break;
            case 37:
                alt18=1;
                break;
            case 33:
                alt18=1;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("424:17: ( rule_attributes[rule] )?", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:424:25: rule_attributes[rule]
                    {
                    following.push(FOLLOW_rule_attributes_in_rule621);
                    rule_attributes(rule);
                    following.pop();


                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_rule631);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:427:17: ( (loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )? ( opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )* )? )?
            int alt25=2;
            int LA25_0 = input.LA(1);
            if ( LA25_0==EOL||LA25_0==19||LA25_0==35||LA25_0==37 ) {
                alt25=1;
            }
            else if ( LA25_0==33 ) {
                alt25=1;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("427:17: ( (loc= \'when\' ( \':\' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )? ( opt_eol loc= \'then\' ( \':\' )? opt_eol ( options {greedy=false; } : any= . )* )? )?", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:427:18: (loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )? ( opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )* )?
                    {
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:427:18: (loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);
                    if ( LA21_0==35 ) {
                        alt21=1;
                    }
                    else if ( LA21_0==EOL||LA21_0==19||LA21_0==33||LA21_0==37 ) {
                        alt21=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("427:18: (loc= \'when\' ( \':\' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] ) )?", 21, 0, input);

                        throw nvae;
                    }
                    switch (alt21) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:427:25: loc= 'when' ( ':' )? opt_eol ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )
                            {
                            loc=(Token)input.LT(1);
                            match(input,35,FOLLOW_35_in_rule640); 
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:427:36: ( ':' )?
                            int alt19=2;
                            int LA19_0 = input.LA(1);
                            if ( LA19_0==36 ) {
                                int LA19_1 = input.LA(2);
                                if ( !( expander != null ) ) {
                                    alt19=1;
                                }
                                else if (  expander != null  ) {
                                    alt19=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("427:36: ( \':\' )?", 19, 1, input);

                                    throw nvae;
                                }
                            }
                            else if ( (LA19_0>=EOL && LA19_0<=35)||(LA19_0>=37 && LA19_0<=65) ) {
                                alt19=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("427:36: ( \':\' )?", 19, 0, input);

                                throw nvae;
                            }
                            switch (alt19) {
                                case 1 :
                                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:427:36: ':'
                                    {
                                    match(input,36,FOLLOW_36_in_rule642); 

                                    }
                                    break;

                            }

                            following.push(FOLLOW_opt_eol_in_rule645);
                            opt_eol();
                            following.pop();

                             
                            				AndDescr lhs = new AndDescr(); rule.setLhs( lhs ); 
                            				lhs.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
                            			
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )
                            int alt20=2;
                            switch ( input.LA(1) ) {
                            case 27:
                                int LA20_1 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 1, input);

                                    throw nvae;
                                }
                                break;
                            case EOL:
                                int LA20_2 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 2, input);

                                    throw nvae;
                                }
                                break;
                            case 19:
                                int LA20_3 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 3, input);

                                    throw nvae;
                                }
                                break;
                            case 37:
                                int LA20_4 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 4, input);

                                    throw nvae;
                                }
                                break;
                            case 33:
                                int LA20_5 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 5, input);

                                    throw nvae;
                                }
                                break;
                            case 60:
                                int LA20_6 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 6, input);

                                    throw nvae;
                                }
                                break;
                            case 61:
                                int LA20_7 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 7, input);

                                    throw nvae;
                                }
                                break;
                            case 62:
                                int LA20_8 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 8, input);

                                    throw nvae;
                                }
                                break;
                            case ID:
                                int LA20_9 = input.LA(2);
                                if (  expander != null  ) {
                                    alt20=1;
                                }
                                else if ( true ) {
                                    alt20=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 9, input);

                                    throw nvae;
                                }
                                break;
                            case INT:
                            case BOOL:
                            case STRING:
                            case FLOAT:
                            case MISC:
                            case WS:
                            case EscapeSequence:
                            case HexDigit:
                            case UnicodeEscape:
                            case OctalEscape:
                            case SH_STYLE_SINGLE_LINE_COMMENT:
                            case C_STYLE_SINGLE_LINE_COMMENT:
                            case MULTI_LINE_COMMENT:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 26:
                            case 28:
                            case 29:
                            case 30:
                            case 31:
                            case 32:
                            case 34:
                            case 35:
                            case 36:
                            case 38:
                            case 39:
                            case 40:
                            case 41:
                            case 42:
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47:
                            case 48:
                            case 49:
                            case 50:
                            case 51:
                            case 52:
                            case 53:
                            case 54:
                            case 55:
                            case 56:
                            case 57:
                            case 58:
                            case 59:
                            case 63:
                            case 64:
                            case 65:
                                alt20=1;
                                break;
                            default:
                                NoViableAltException nvae =
                                    new NoViableAltException("432:25: ({...}? expander_lhs_block[lhs] | normal_lhs_block[lhs] )", 20, 0, input);

                                throw nvae;
                            }

                            switch (alt20) {
                                case 1 :
                                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:433:33: {...}? expander_lhs_block[lhs]
                                    {
                                    if ( !( expander != null ) ) {
                                        throw new FailedPredicateException(input, "rule", " expander != null ");
                                    }
                                    following.push(FOLLOW_expander_lhs_block_in_rule663);
                                    expander_lhs_block(lhs);
                                    following.pop();


                                    }
                                    break;
                                case 2 :
                                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:434:35: normal_lhs_block[lhs]
                                    {
                                    following.push(FOLLOW_normal_lhs_block_in_rule672);
                                    normal_lhs_block(lhs);
                                    following.pop();


                                    }
                                    break;

                            }


                            }
                            break;

                    }

                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:438:17: ( opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )* )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);
                    if ( LA24_0==EOL||LA24_0==19||LA24_0==37 ) {
                        alt24=1;
                    }
                    else if ( LA24_0==33 ) {
                        alt24=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("438:17: ( opt_eol loc= \'then\' ( \':\' )? opt_eol ( options {greedy=false; } : any= . )* )?", 24, 0, input);

                        throw nvae;
                    }
                    switch (alt24) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:438:19: opt_eol loc= 'then' ( ':' )? opt_eol ( options {greedy=false; } : any= . )*
                            {
                            following.push(FOLLOW_opt_eol_in_rule695);
                            opt_eol();
                            following.pop();

                            loc=(Token)input.LT(1);
                            match(input,37,FOLLOW_37_in_rule699); 
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:438:38: ( ':' )?
                            int alt22=2;
                            int LA22_0 = input.LA(1);
                            if ( LA22_0==36 ) {
                                alt22=1;
                            }
                            else if ( (LA22_0>=EOL && LA22_0<=35)||(LA22_0>=37 && LA22_0<=65) ) {
                                alt22=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("438:38: ( \':\' )?", 22, 0, input);

                                throw nvae;
                            }
                            switch (alt22) {
                                case 1 :
                                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:438:38: ':'
                                    {
                                    match(input,36,FOLLOW_36_in_rule701); 

                                    }
                                    break;

                            }

                            following.push(FOLLOW_opt_eol_in_rule705);
                            opt_eol();
                            following.pop();

                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:439:25: ( options {greedy=false; } : any= . )*
                            loop23:
                            do {
                                int alt23=2;
                                int LA23_0 = input.LA(1);
                                if ( LA23_0==33 ) {
                                    alt23=2;
                                }
                                else if ( (LA23_0>=EOL && LA23_0<=32)||(LA23_0>=34 && LA23_0<=65) ) {
                                    alt23=1;
                                }


                                switch (alt23) {
                            	case 1 :
                            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:439:52: any= .
                            	    {
                            	    any=(Token)input.LT(1);
                            	    matchAny(input); 
                            	    
                            	    					consequence = consequence + " " + any.getText();
                            	    				

                            	    }
                            	    break;

                            	default :
                            	    break loop23;
                                }
                            } while (true);

                            
                            				if ( expander != null ) {
                            					String expanded = runThenExpander( consequence, offset(loc.getLine()) );
                            					rule.setConsequence( expanded );
                            				} else { 
                            					rule.setConsequence( consequence ); 
                            				}
                            				rule.setConsequenceLocation(offset(loc.getLine()), loc.getCharPositionInLine());
                            			

                            }
                            break;

                    }


                    }
                    break;

            }

            match(input,33,FOLLOW_33_in_rule751); 
            following.push(FOLLOW_opt_eol_in_rule753);
            opt_eol();
            following.pop();

            
            			debug( "end rule: " + ruleName );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return rule;
    }
    // $ANTLR end rule


    // $ANTLR start extra_statement
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:460:1: extra_statement : ( import_statement | global | function ) ;
    public void extra_statement() throws RecognitionException {   
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:462:9: ( ( import_statement | global | function ) )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:462:9: ( import_statement | global | function )
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:462:9: ( import_statement | global | function )
            int alt26=3;
            switch ( input.LA(1) ) {
            case 21:
                alt26=1;
                break;
            case 25:
                alt26=2;
                break;
            case 26:
                alt26=3;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("462:9: ( import_statement | global | function )", 26, 0, input);

                throw nvae;
            }

            switch (alt26) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:462:17: import_statement
                    {
                    following.push(FOLLOW_import_statement_in_extra_statement773);
                    import_statement();
                    following.pop();


                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:463:17: global
                    {
                    following.push(FOLLOW_global_in_extra_statement778);
                    global();
                    following.pop();


                    }
                    break;
                case 3 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:464:17: function
                    {
                    following.push(FOLLOW_function_in_extra_statement783);
                    function();
                    following.pop();


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end extra_statement


    // $ANTLR start rule_attributes
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:468:1: rule_attributes[RuleDescr rule] : ( 'attributes' )? ( ':' )? opt_eol ( ( ',' )? a= rule_attribute opt_eol )* ;
    public void rule_attributes(RuleDescr rule) throws RecognitionException {   
        AttributeDescr a = null;


        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:470:25: ( ( 'attributes' )? ( ':' )? opt_eol ( ( ',' )? a= rule_attribute opt_eol )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:470:25: ( 'attributes' )? ( ':' )? opt_eol ( ( ',' )? a= rule_attribute opt_eol )*
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:470:25: ( 'attributes' )?
            int alt27=2;
            int LA27_0 = input.LA(1);
            if ( LA27_0==38 ) {
                alt27=1;
            }
            else if ( LA27_0==EOL||LA27_0==19||LA27_0==28||LA27_0==33||(LA27_0>=35 && LA27_0<=37)||(LA27_0>=39 && LA27_0<=44) ) {
                alt27=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("470:25: ( \'attributes\' )?", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:470:25: 'attributes'
                    {
                    match(input,38,FOLLOW_38_in_rule_attributes802); 

                    }
                    break;

            }

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:470:39: ( ':' )?
            int alt28=2;
            int LA28_0 = input.LA(1);
            if ( LA28_0==36 ) {
                alt28=1;
            }
            else if ( LA28_0==EOL||LA28_0==19||LA28_0==28||LA28_0==33||LA28_0==35||LA28_0==37||(LA28_0>=39 && LA28_0<=44) ) {
                alt28=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("470:39: ( \':\' )?", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:470:39: ':'
                    {
                    match(input,36,FOLLOW_36_in_rule_attributes805); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_rule_attributes808);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:471:25: ( ( ',' )? a= rule_attribute opt_eol )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);
                if ( LA30_0==28||(LA30_0>=39 && LA30_0<=44) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:471:33: ( ',' )? a= rule_attribute opt_eol
            	    {
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:471:33: ( ',' )?
            	    int alt29=2;
            	    int LA29_0 = input.LA(1);
            	    if ( LA29_0==28 ) {
            	        alt29=1;
            	    }
            	    else if ( (LA29_0>=39 && LA29_0<=44) ) {
            	        alt29=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("471:33: ( \',\' )?", 29, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt29) {
            	        case 1 :
            	            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:471:33: ','
            	            {
            	            match(input,28,FOLLOW_28_in_rule_attributes815); 

            	            }
            	            break;

            	    }

            	    following.push(FOLLOW_rule_attribute_in_rule_attributes820);
            	    a=rule_attribute();
            	    following.pop();

            	    following.push(FOLLOW_opt_eol_in_rule_attributes822);
            	    opt_eol();
            	    following.pop();

            	    
            	    					rule.addAttribute( a );
            	    				

            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end rule_attributes


    // $ANTLR start rule_attribute
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:478:1: rule_attribute returns [AttributeDescr d] : (a= salience | a= no_loop | a= agenda_group | a= duration | a= activation_group | a= auto_focus );
    public AttributeDescr rule_attribute() throws RecognitionException {   
        AttributeDescr d;
        AttributeDescr a = null;


        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:483:25: (a= salience | a= no_loop | a= agenda_group | a= duration | a= activation_group | a= auto_focus )
            int alt31=6;
            switch ( input.LA(1) ) {
            case 39:
                alt31=1;
                break;
            case 40:
                alt31=2;
                break;
            case 43:
                alt31=3;
                break;
            case 44:
                alt31=4;
                break;
            case 42:
                alt31=5;
                break;
            case 41:
                alt31=6;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("478:1: rule_attribute returns [AttributeDescr d] : (a= salience | a= no_loop | a= agenda_group | a= duration | a= activation_group | a= auto_focus );", 31, 0, input);

                throw nvae;
            }

            switch (alt31) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:483:25: a= salience
                    {
                    following.push(FOLLOW_salience_in_rule_attribute861);
                    a=salience();
                    following.pop();

                     d = a; 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:484:25: a= no_loop
                    {
                    following.push(FOLLOW_no_loop_in_rule_attribute871);
                    a=no_loop();
                    following.pop();

                     d = a; 

                    }
                    break;
                case 3 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:485:25: a= agenda_group
                    {
                    following.push(FOLLOW_agenda_group_in_rule_attribute882);
                    a=agenda_group();
                    following.pop();

                     d = a; 

                    }
                    break;
                case 4 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:486:25: a= duration
                    {
                    following.push(FOLLOW_duration_in_rule_attribute895);
                    a=duration();
                    following.pop();

                     d = a; 

                    }
                    break;
                case 5 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:487:25: a= activation_group
                    {
                    following.push(FOLLOW_activation_group_in_rule_attribute909);
                    a=activation_group();
                    following.pop();

                     d = a; 

                    }
                    break;
                case 6 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:488:25: a= auto_focus
                    {
                    following.push(FOLLOW_auto_focus_in_rule_attribute920);
                    a=auto_focus();
                    following.pop();

                     d = a; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end rule_attribute


    // $ANTLR start salience
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:492:1: salience returns [AttributeDescr d ] : loc= 'salience' opt_eol i= INT ( ';' )? opt_eol ;
    public AttributeDescr salience() throws RecognitionException {   
        AttributeDescr d;
        Token loc=null;
        Token i=null;

        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:497:17: (loc= 'salience' opt_eol i= INT ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:497:17: loc= 'salience' opt_eol i= INT ( ';' )? opt_eol
            {
            loc=(Token)input.LT(1);
            match(input,39,FOLLOW_39_in_salience953); 
            following.push(FOLLOW_opt_eol_in_salience955);
            opt_eol();
            following.pop();

            i=(Token)input.LT(1);
            match(input,INT,FOLLOW_INT_in_salience959); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:497:46: ( ';' )?
            int alt32=2;
            int LA32_0 = input.LA(1);
            if ( LA32_0==19 ) {
                alt32=1;
            }
            else if ( LA32_0==EOL||LA32_0==28||LA32_0==33||LA32_0==35||LA32_0==37||(LA32_0>=39 && LA32_0<=44) ) {
                alt32=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("497:46: ( \';\' )?", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:497:46: ';'
                    {
                    match(input,19,FOLLOW_19_in_salience961); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_salience964);
            opt_eol();
            following.pop();

            
            			d = new AttributeDescr( "salience", i.getText() );
            			d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end salience


    // $ANTLR start no_loop
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:504:1: no_loop returns [AttributeDescr d] : ( (loc= 'no-loop' opt_eol ( ';' )? opt_eol ) | (loc= 'no-loop' t= BOOL opt_eol ( ';' )? opt_eol ) );
    public AttributeDescr no_loop() throws RecognitionException {   
        AttributeDescr d;
        Token loc=null;
        Token t=null;

        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:509:17: ( (loc= 'no-loop' opt_eol ( ';' )? opt_eol ) | (loc= 'no-loop' t= BOOL opt_eol ( ';' )? opt_eol ) )
            int alt35=2;
            int LA35_0 = input.LA(1);
            if ( LA35_0==40 ) {
                int LA35_1 = input.LA(2);
                if ( LA35_1==BOOL ) {
                    alt35=2;
                }
                else if ( LA35_1==EOL||LA35_1==19||LA35_1==28||LA35_1==33||LA35_1==35||LA35_1==37||(LA35_1>=39 && LA35_1<=44) ) {
                    alt35=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("504:1: no_loop returns [AttributeDescr d] : ( (loc= \'no-loop\' opt_eol ( \';\' )? opt_eol ) | (loc= \'no-loop\' t= BOOL opt_eol ( \';\' )? opt_eol ) );", 35, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("504:1: no_loop returns [AttributeDescr d] : ( (loc= \'no-loop\' opt_eol ( \';\' )? opt_eol ) | (loc= \'no-loop\' t= BOOL opt_eol ( \';\' )? opt_eol ) );", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:509:17: (loc= 'no-loop' opt_eol ( ';' )? opt_eol )
                    {
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:509:17: (loc= 'no-loop' opt_eol ( ';' )? opt_eol )
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:510:25: loc= 'no-loop' opt_eol ( ';' )? opt_eol
                    {
                    loc=(Token)input.LT(1);
                    match(input,40,FOLLOW_40_in_no_loop999); 
                    following.push(FOLLOW_opt_eol_in_no_loop1001);
                    opt_eol();
                    following.pop();

                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:510:47: ( ';' )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);
                    if ( LA33_0==19 ) {
                        alt33=1;
                    }
                    else if ( LA33_0==EOL||LA33_0==28||LA33_0==33||LA33_0==35||LA33_0==37||(LA33_0>=39 && LA33_0<=44) ) {
                        alt33=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("510:47: ( \';\' )?", 33, 0, input);

                        throw nvae;
                    }
                    switch (alt33) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:510:47: ';'
                            {
                            match(input,19,FOLLOW_19_in_no_loop1003); 

                            }
                            break;

                    }

                    following.push(FOLLOW_opt_eol_in_no_loop1006);
                    opt_eol();
                    following.pop();

                    
                    				d = new AttributeDescr( "no-loop", "true" );
                    				d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
                    			

                    }


                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:517:17: (loc= 'no-loop' t= BOOL opt_eol ( ';' )? opt_eol )
                    {
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:517:17: (loc= 'no-loop' t= BOOL opt_eol ( ';' )? opt_eol )
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:518:25: loc= 'no-loop' t= BOOL opt_eol ( ';' )? opt_eol
                    {
                    loc=(Token)input.LT(1);
                    match(input,40,FOLLOW_40_in_no_loop1031); 
                    t=(Token)input.LT(1);
                    match(input,BOOL,FOLLOW_BOOL_in_no_loop1035); 
                    following.push(FOLLOW_opt_eol_in_no_loop1037);
                    opt_eol();
                    following.pop();

                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:518:54: ( ';' )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);
                    if ( LA34_0==19 ) {
                        alt34=1;
                    }
                    else if ( LA34_0==EOL||LA34_0==28||LA34_0==33||LA34_0==35||LA34_0==37||(LA34_0>=39 && LA34_0<=44) ) {
                        alt34=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("518:54: ( \';\' )?", 34, 0, input);

                        throw nvae;
                    }
                    switch (alt34) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:518:54: ';'
                            {
                            match(input,19,FOLLOW_19_in_no_loop1039); 

                            }
                            break;

                    }

                    following.push(FOLLOW_opt_eol_in_no_loop1042);
                    opt_eol();
                    following.pop();

                    
                    				d = new AttributeDescr( "no-loop", t.getText() );
                    				d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
                    			

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end no_loop


    // $ANTLR start auto_focus
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:528:1: auto_focus returns [AttributeDescr d] : ( (loc= 'auto-focus' opt_eol ( ';' )? opt_eol ) | (loc= 'auto-focus' t= BOOL opt_eol ( ';' )? opt_eol ) );
    public AttributeDescr auto_focus() throws RecognitionException {   
        AttributeDescr d;
        Token loc=null;
        Token t=null;

        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:533:17: ( (loc= 'auto-focus' opt_eol ( ';' )? opt_eol ) | (loc= 'auto-focus' t= BOOL opt_eol ( ';' )? opt_eol ) )
            int alt38=2;
            int LA38_0 = input.LA(1);
            if ( LA38_0==41 ) {
                int LA38_1 = input.LA(2);
                if ( LA38_1==BOOL ) {
                    alt38=2;
                }
                else if ( LA38_1==EOL||LA38_1==19||LA38_1==28||LA38_1==33||LA38_1==35||LA38_1==37||(LA38_1>=39 && LA38_1<=44) ) {
                    alt38=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("528:1: auto_focus returns [AttributeDescr d] : ( (loc= \'auto-focus\' opt_eol ( \';\' )? opt_eol ) | (loc= \'auto-focus\' t= BOOL opt_eol ( \';\' )? opt_eol ) );", 38, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("528:1: auto_focus returns [AttributeDescr d] : ( (loc= \'auto-focus\' opt_eol ( \';\' )? opt_eol ) | (loc= \'auto-focus\' t= BOOL opt_eol ( \';\' )? opt_eol ) );", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:533:17: (loc= 'auto-focus' opt_eol ( ';' )? opt_eol )
                    {
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:533:17: (loc= 'auto-focus' opt_eol ( ';' )? opt_eol )
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:534:25: loc= 'auto-focus' opt_eol ( ';' )? opt_eol
                    {
                    loc=(Token)input.LT(1);
                    match(input,41,FOLLOW_41_in_auto_focus1088); 
                    following.push(FOLLOW_opt_eol_in_auto_focus1090);
                    opt_eol();
                    following.pop();

                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:534:50: ( ';' )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);
                    if ( LA36_0==19 ) {
                        alt36=1;
                    }
                    else if ( LA36_0==EOL||LA36_0==28||LA36_0==33||LA36_0==35||LA36_0==37||(LA36_0>=39 && LA36_0<=44) ) {
                        alt36=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("534:50: ( \';\' )?", 36, 0, input);

                        throw nvae;
                    }
                    switch (alt36) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:534:50: ';'
                            {
                            match(input,19,FOLLOW_19_in_auto_focus1092); 

                            }
                            break;

                    }

                    following.push(FOLLOW_opt_eol_in_auto_focus1095);
                    opt_eol();
                    following.pop();

                    
                    				d = new AttributeDescr( "auto-focus", "true" );
                    				d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
                    			

                    }


                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:541:17: (loc= 'auto-focus' t= BOOL opt_eol ( ';' )? opt_eol )
                    {
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:541:17: (loc= 'auto-focus' t= BOOL opt_eol ( ';' )? opt_eol )
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:542:25: loc= 'auto-focus' t= BOOL opt_eol ( ';' )? opt_eol
                    {
                    loc=(Token)input.LT(1);
                    match(input,41,FOLLOW_41_in_auto_focus1120); 
                    t=(Token)input.LT(1);
                    match(input,BOOL,FOLLOW_BOOL_in_auto_focus1124); 
                    following.push(FOLLOW_opt_eol_in_auto_focus1126);
                    opt_eol();
                    following.pop();

                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:542:57: ( ';' )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);
                    if ( LA37_0==19 ) {
                        alt37=1;
                    }
                    else if ( LA37_0==EOL||LA37_0==28||LA37_0==33||LA37_0==35||LA37_0==37||(LA37_0>=39 && LA37_0<=44) ) {
                        alt37=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("542:57: ( \';\' )?", 37, 0, input);

                        throw nvae;
                    }
                    switch (alt37) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:542:57: ';'
                            {
                            match(input,19,FOLLOW_19_in_auto_focus1128); 

                            }
                            break;

                    }

                    following.push(FOLLOW_opt_eol_in_auto_focus1131);
                    opt_eol();
                    following.pop();

                    
                    				d = new AttributeDescr( "auto-focus", t.getText() );
                    				d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
                    			

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end auto_focus


    // $ANTLR start activation_group
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:552:1: activation_group returns [AttributeDescr d] : loc= 'activation-group' opt_eol name= STRING ( ';' )? opt_eol ;
    public AttributeDescr activation_group() throws RecognitionException {   
        AttributeDescr d;
        Token loc=null;
        Token name=null;

        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:557:17: (loc= 'activation-group' opt_eol name= STRING ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:557:17: loc= 'activation-group' opt_eol name= STRING ( ';' )? opt_eol
            {
            loc=(Token)input.LT(1);
            match(input,42,FOLLOW_42_in_activation_group1173); 
            following.push(FOLLOW_opt_eol_in_activation_group1175);
            opt_eol();
            following.pop();

            name=(Token)input.LT(1);
            match(input,STRING,FOLLOW_STRING_in_activation_group1179); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:557:60: ( ';' )?
            int alt39=2;
            int LA39_0 = input.LA(1);
            if ( LA39_0==19 ) {
                alt39=1;
            }
            else if ( LA39_0==EOL||LA39_0==28||LA39_0==33||LA39_0==35||LA39_0==37||(LA39_0>=39 && LA39_0<=44) ) {
                alt39=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("557:60: ( \';\' )?", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:557:60: ';'
                    {
                    match(input,19,FOLLOW_19_in_activation_group1181); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_activation_group1184);
            opt_eol();
            following.pop();

            
            			d = new AttributeDescr( "activation-group", getString( name ) );
            			d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end activation_group


    // $ANTLR start agenda_group
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:564:1: agenda_group returns [AttributeDescr d] : loc= 'agenda-group' opt_eol name= STRING ( ';' )? opt_eol ;
    public AttributeDescr agenda_group() throws RecognitionException {   
        AttributeDescr d;
        Token loc=null;
        Token name=null;

        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:569:17: (loc= 'agenda-group' opt_eol name= STRING ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:569:17: loc= 'agenda-group' opt_eol name= STRING ( ';' )? opt_eol
            {
            loc=(Token)input.LT(1);
            match(input,43,FOLLOW_43_in_agenda_group1213); 
            following.push(FOLLOW_opt_eol_in_agenda_group1215);
            opt_eol();
            following.pop();

            name=(Token)input.LT(1);
            match(input,STRING,FOLLOW_STRING_in_agenda_group1219); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:569:56: ( ';' )?
            int alt40=2;
            int LA40_0 = input.LA(1);
            if ( LA40_0==19 ) {
                alt40=1;
            }
            else if ( LA40_0==EOL||LA40_0==28||LA40_0==33||LA40_0==35||LA40_0==37||(LA40_0>=39 && LA40_0<=44) ) {
                alt40=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("569:56: ( \';\' )?", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:569:56: ';'
                    {
                    match(input,19,FOLLOW_19_in_agenda_group1221); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_agenda_group1224);
            opt_eol();
            following.pop();

            
            			d = new AttributeDescr( "agenda-group", getString( name ) );
            			d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end agenda_group


    // $ANTLR start duration
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:577:1: duration returns [AttributeDescr d] : loc= 'duration' opt_eol i= INT ( ';' )? opt_eol ;
    public AttributeDescr duration() throws RecognitionException {   
        AttributeDescr d;
        Token loc=null;
        Token i=null;

        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:582:17: (loc= 'duration' opt_eol i= INT ( ';' )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:582:17: loc= 'duration' opt_eol i= INT ( ';' )? opt_eol
            {
            loc=(Token)input.LT(1);
            match(input,44,FOLLOW_44_in_duration1256); 
            following.push(FOLLOW_opt_eol_in_duration1258);
            opt_eol();
            following.pop();

            i=(Token)input.LT(1);
            match(input,INT,FOLLOW_INT_in_duration1262); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:582:46: ( ';' )?
            int alt41=2;
            int LA41_0 = input.LA(1);
            if ( LA41_0==19 ) {
                alt41=1;
            }
            else if ( LA41_0==EOL||LA41_0==28||LA41_0==33||LA41_0==35||LA41_0==37||(LA41_0>=39 && LA41_0<=44) ) {
                alt41=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("582:46: ( \';\' )?", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:582:46: ';'
                    {
                    match(input,19,FOLLOW_19_in_duration1264); 

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_duration1267);
            opt_eol();
            following.pop();

            
            			d = new AttributeDescr( "duration", i.getText() );
            			d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end duration


    // $ANTLR start normal_lhs_block
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:590:1: normal_lhs_block[AndDescr descrs] : (d= lhs opt_eol )* opt_eol ;
    public void normal_lhs_block(AndDescr descrs) throws RecognitionException {   
        PatternDescr d = null;


        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:592:17: ( (d= lhs opt_eol )* opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:592:17: (d= lhs opt_eol )* opt_eol
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:592:17: (d= lhs opt_eol )*
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);
                if ( LA42_0==ID||LA42_0==27||(LA42_0>=60 && LA42_0<=62) ) {
                    alt42=1;
                }


                switch (alt42) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:592:25: d= lhs opt_eol
            	    {
            	    following.push(FOLLOW_lhs_in_normal_lhs_block1293);
            	    d=lhs();
            	    following.pop();

            	    following.push(FOLLOW_opt_eol_in_normal_lhs_block1295);
            	    opt_eol();
            	    following.pop();

            	     descrs.addDescr( d ); 

            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);

            following.push(FOLLOW_opt_eol_in_normal_lhs_block1307);
            opt_eol();
            following.pop();


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end normal_lhs_block


    // $ANTLR start expander_lhs_block
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:600:1: expander_lhs_block[AndDescr descrs] : ( options {greedy=false; } : text= paren_chunk loc= EOL ( EOL )* )* ;
    public void expander_lhs_block(AndDescr descrs) throws RecognitionException {   
        Token loc=null;
        String text = null;


        
        		String lhsBlock = null;
        		String eol = System.getProperty( "line.separator" );
        		List constraints = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:607:17: ( ( options {greedy=false; } : text= paren_chunk loc= EOL ( EOL )* )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:607:17: ( options {greedy=false; } : text= paren_chunk loc= EOL ( EOL )* )*
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:607:17: ( options {greedy=false; } : text= paren_chunk loc= EOL ( EOL )* )*
            loop44:
            do {
                int alt44=2;
                switch ( input.LA(1) ) {
                case 33:
                    alt44=2;
                    break;
                case EOL:
                    alt44=2;
                    break;
                case 37:
                    alt44=2;
                    break;
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 34:
                case 35:
                case 36:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    alt44=1;
                    break;
                case 19:
                    alt44=2;
                    break;

                }

                switch (alt44) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:608:25: text= paren_chunk loc= EOL ( EOL )*
            	    {
            	    following.push(FOLLOW_paren_chunk_in_expander_lhs_block1348);
            	    text=paren_chunk();
            	    following.pop();

            	    loc=(Token)input.LT(1);
            	    match(input,EOL,FOLLOW_EOL_in_expander_lhs_block1352); 
            	    
            	    				//only expand non null
            	    				if (text != null) {
            	    					if (text.trim().startsWith("-")) {
            	    						if (constraints == null) {
            	    							constraints = new ArrayList();
            	    						}
            	    						constraints.add(runWhenExpander( text, offset(loc.getLine())));
            	    					} else {
            	    						if (constraints != null) {
            	    							lhsBlock = applyConstraints(constraints, lhsBlock);
            	    							constraints = null;
            	    						}
            	    					
            	    					
            	    						if (lhsBlock == null) {					
            	    							lhsBlock = runWhenExpander( text, offset(loc.getLine()));
            	    						} else {
            	    							lhsBlock = lhsBlock + eol + runWhenExpander( text, offset(loc.getLine()));
            	    						}
            	    					}
            	    					text = null;
            	    				}
            	    			
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:635:18: ( EOL )*
            	    loop43:
            	    do {
            	        int alt43=2;
            	        int LA43_0 = input.LA(1);
            	        if ( LA43_0==EOL ) {
            	            alt43=1;
            	        }


            	        switch (alt43) {
            	    	case 1 :
            	    	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:635:19: EOL
            	    	    {
            	    	    match(input,EOL,FOLLOW_EOL_in_expander_lhs_block1372); 

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop43;
            	        }
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);

            	
            			//flush out any constraints left handing before the RHS
            			lhsBlock = applyConstraints(constraints, lhsBlock);
            			if (lhsBlock != null) {
            				reparseLhs(lhsBlock, descrs);
            			}
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end expander_lhs_block


    // $ANTLR start lhs
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:650:1: lhs returns [PatternDescr d] : l= lhs_or ;
    public PatternDescr lhs() throws RecognitionException {   
        PatternDescr d;
        PatternDescr l = null;


        
        		d=null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:654:17: (l= lhs_or )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:654:17: l= lhs_or
            {
            following.push(FOLLOW_lhs_or_in_lhs1415);
            l=lhs_or();
            following.pop();

             d = l; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs


    // $ANTLR start lhs_column
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:658:1: lhs_column returns [PatternDescr d] : (f= fact_binding | f= fact );
    public PatternDescr lhs_column() throws RecognitionException {   
        PatternDescr d;
        PatternDescr f = null;


        
        		d=null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:662:17: (f= fact_binding | f= fact )
            int alt45=2;
            alt45 = dfa45.predict(input); 
            switch (alt45) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:662:17: f= fact_binding
                    {
                    following.push(FOLLOW_fact_binding_in_lhs_column1443);
                    f=fact_binding();
                    following.pop();

                     d = f; 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:663:17: f= fact
                    {
                    following.push(FOLLOW_fact_in_lhs_column1452);
                    f=fact();
                    following.pop();

                     d = f; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_column


    // $ANTLR start fact_binding
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:666:1: fact_binding returns [PatternDescr d] : id= ID opt_eol ':' opt_eol fe= fact_expression[id.getText()] ;
    public PatternDescr fact_binding() throws RecognitionException {   
        PatternDescr d;
        Token id=null;
        PatternDescr fe = null;


        
        		d=null;
        		boolean multi=false;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:672:17: (id= ID opt_eol ':' opt_eol fe= fact_expression[id.getText()] )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:672:17: id= ID opt_eol ':' opt_eol fe= fact_expression[id.getText()]
            {
            id=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_fact_binding1484); 
            following.push(FOLLOW_opt_eol_in_fact_binding1494);
            opt_eol();
            following.pop();

            match(input,36,FOLLOW_36_in_fact_binding1496); 
            following.push(FOLLOW_opt_eol_in_fact_binding1498);
            opt_eol();
            following.pop();

            following.push(FOLLOW_fact_expression_in_fact_binding1502);
            fe=fact_expression(id.getText());
            following.pop();

            
             			d=fe;
             		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end fact_binding


    // $ANTLR start fact_expression
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:680:2: fact_expression[String id] returns [PatternDescr pd] : ( '(' opt_eol fe= fact_expression[id] opt_eol ')' | f= fact opt_eol ( ('or'|'||') opt_eol f= fact )* );
    public PatternDescr fact_expression(String id) throws RecognitionException {   
        PatternDescr pd;
        PatternDescr fe = null;

        PatternDescr f = null;


        
         		pd = null;
         		boolean multi = false;
         	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:685:17: ( '(' opt_eol fe= fact_expression[id] opt_eol ')' | f= fact opt_eol ( ('or'|'||') opt_eol f= fact )* )
            int alt47=2;
            int LA47_0 = input.LA(1);
            if ( LA47_0==27 ) {
                alt47=1;
            }
            else if ( LA47_0==ID ) {
                alt47=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("680:2: fact_expression[String id] returns [PatternDescr pd] : ( \'(\' opt_eol fe= fact_expression[id] opt_eol \')\' | f= fact opt_eol ( (\'or\'|\'||\') opt_eol f= fact )* );", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:685:17: '(' opt_eol fe= fact_expression[id] opt_eol ')'
                    {
                    match(input,27,FOLLOW_27_in_fact_expression1534); 
                    following.push(FOLLOW_opt_eol_in_fact_expression1536);
                    opt_eol();
                    following.pop();

                    following.push(FOLLOW_fact_expression_in_fact_expression1540);
                    fe=fact_expression(id);
                    following.pop();

                    following.push(FOLLOW_opt_eol_in_fact_expression1542);
                    opt_eol();
                    following.pop();

                    match(input,29,FOLLOW_29_in_fact_expression1544); 
                     pd=fe; 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:686:17: f= fact opt_eol ( ('or'|'||') opt_eol f= fact )*
                    {
                    following.push(FOLLOW_fact_in_fact_expression1555);
                    f=fact();
                    following.pop();

                    following.push(FOLLOW_opt_eol_in_fact_expression1557);
                    opt_eol();
                    following.pop();

                    
                     			((ColumnDescr)f).setIdentifier( id );
                     			pd = f;
                     		
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:691:17: ( ('or'|'||') opt_eol f= fact )*
                    loop46:
                    do {
                        int alt46=2;
                        int LA46_0 = input.LA(1);
                        if ( (LA46_0>=45 && LA46_0<=46) ) {
                            alt46=1;
                        }


                        switch (alt46) {
                    	case 1 :
                    	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:691:25: ('or'|'||') opt_eol f= fact
                    	    {
                    	    if ( (input.LA(1)>=45 && input.LA(1)<=46) ) {
                    	        input.consume();
                    	        errorRecovery=false;
                    	    }
                    	    else {
                    	        MismatchedSetException mse =
                    	            new MismatchedSetException(null,input);
                    	        recoverFromMismatchedSet(input,mse,FOLLOW_set_in_fact_expression1570);    throw mse;
                    	    }

                    	    following.push(FOLLOW_opt_eol_in_fact_expression1575);
                    	    opt_eol();
                    	    following.pop();

                    	    	if ( ! multi ) {
                    	     					PatternDescr first = pd;
                    	     					pd = new OrDescr();
                    	     					((OrDescr)pd).addDescr( first );
                    	     					multi=true;
                    	     				}
                    	     			
                    	    following.push(FOLLOW_fact_in_fact_expression1589);
                    	    f=fact();
                    	    following.pop();

                    	    
                    	     				((ColumnDescr)f).setIdentifier( id );
                    	     				((OrDescr)pd).addDescr( f );
                    	     			

                    	    }
                    	    break;

                    	default :
                    	    break loop46;
                        }
                    } while (true);


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return pd;
    }
    // $ANTLR end fact_expression


    // $ANTLR start fact
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:707:1: fact returns [PatternDescr d] : id= dotted_name opt_eol loc= '(' opt_eol (c= constraints )? opt_eol endLoc= ')' opt_eol ;
    public PatternDescr fact() throws RecognitionException {   
        PatternDescr d;
        Token loc=null;
        Token endLoc=null;
        String id = null;

        List c = null;


        
        		d=null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:711:17: (id= dotted_name opt_eol loc= '(' opt_eol (c= constraints )? opt_eol endLoc= ')' opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:711:17: id= dotted_name opt_eol loc= '(' opt_eol (c= constraints )? opt_eol endLoc= ')' opt_eol
            {
            following.push(FOLLOW_dotted_name_in_fact1628);
            id=dotted_name();
            following.pop();

             
             			d = new ColumnDescr( id ); 
             		
            following.push(FOLLOW_opt_eol_in_fact1636);
            opt_eol();
            following.pop();

            loc=(Token)input.LT(1);
            match(input,27,FOLLOW_27_in_fact1644); 
            
             				d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
             			
            following.push(FOLLOW_opt_eol_in_fact1647);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:717:34: (c= constraints )?
            int alt48=2;
            alt48 = dfa48.predict(input); 
            switch (alt48) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:717:41: c= constraints
                    {
                    following.push(FOLLOW_constraints_in_fact1653);
                    c=constraints();
                    following.pop();

                    
                    		 			for ( Iterator cIter = c.iterator() ; cIter.hasNext() ; ) {
                     						((ColumnDescr)d).addDescr( (PatternDescr) cIter.next() );
                     					}
                     				

                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_fact1672);
            opt_eol();
            following.pop();

            endLoc=(Token)input.LT(1);
            match(input,29,FOLLOW_29_in_fact1676); 
            following.push(FOLLOW_opt_eol_in_fact1678);
            opt_eol();
            following.pop();

            
             					d.setEndLocation( offset(endLoc.getLine()), endLoc.getCharPositionInLine() );	
             				

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end fact


    // $ANTLR start constraints
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:731:1: constraints returns [List constraints] : opt_eol ( constraint[constraints] | predicate[constraints] ) ( opt_eol ',' opt_eol ( constraint[constraints] | predicate[constraints] ) )* opt_eol ;
    public List constraints() throws RecognitionException {   
        List constraints;
        
        		constraints = new ArrayList();
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:735:17: ( opt_eol ( constraint[constraints] | predicate[constraints] ) ( opt_eol ',' opt_eol ( constraint[constraints] | predicate[constraints] ) )* opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:735:17: opt_eol ( constraint[constraints] | predicate[constraints] ) ( opt_eol ',' opt_eol ( constraint[constraints] | predicate[constraints] ) )* opt_eol
            {
            following.push(FOLLOW_opt_eol_in_constraints1710);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:736:17: ( constraint[constraints] | predicate[constraints] )
            int alt49=2;
            int LA49_0 = input.LA(1);
            if ( LA49_0==EOL||LA49_0==19 ) {
                alt49=1;
            }
            else if ( LA49_0==ID ) {
                int LA49_2 = input.LA(2);
                if ( LA49_2==36 ) {
                    int LA49_3 = input.LA(3);
                    if ( LA49_3==ID ) {
                        int LA49_8 = input.LA(4);
                        if ( LA49_8==57 ) {
                            alt49=2;
                        }
                        else if ( LA49_8==EOL||LA49_8==19||(LA49_8>=28 && LA49_8<=29)||(LA49_8>=47 && LA49_8<=55) ) {
                            alt49=1;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("736:17: ( constraint[constraints] | predicate[constraints] )", 49, 8, input);

                            throw nvae;
                        }
                    }
                    else if ( LA49_3==EOL||LA49_3==19 ) {
                        alt49=1;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("736:17: ( constraint[constraints] | predicate[constraints] )", 49, 3, input);

                        throw nvae;
                    }
                }
                else if ( LA49_2==EOL||LA49_2==19||(LA49_2>=28 && LA49_2<=29)||(LA49_2>=47 && LA49_2<=55) ) {
                    alt49=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("736:17: ( constraint[constraints] | predicate[constraints] )", 49, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("736:17: ( constraint[constraints] | predicate[constraints] )", 49, 0, input);

                throw nvae;
            }
            switch (alt49) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:736:18: constraint[constraints]
                    {
                    following.push(FOLLOW_constraint_in_constraints1715);
                    constraint(constraints);
                    following.pop();


                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:736:42: predicate[constraints]
                    {
                    following.push(FOLLOW_predicate_in_constraints1718);
                    predicate(constraints);
                    following.pop();


                    }
                    break;

            }

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:737:17: ( opt_eol ',' opt_eol ( constraint[constraints] | predicate[constraints] ) )*
            loop51:
            do {
                int alt51=2;
                alt51 = dfa51.predict(input); 
                switch (alt51) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:737:19: opt_eol ',' opt_eol ( constraint[constraints] | predicate[constraints] )
            	    {
            	    following.push(FOLLOW_opt_eol_in_constraints1726);
            	    opt_eol();
            	    following.pop();

            	    match(input,28,FOLLOW_28_in_constraints1728); 
            	    following.push(FOLLOW_opt_eol_in_constraints1730);
            	    opt_eol();
            	    following.pop();

            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:737:39: ( constraint[constraints] | predicate[constraints] )
            	    int alt50=2;
            	    int LA50_0 = input.LA(1);
            	    if ( LA50_0==EOL||LA50_0==19 ) {
            	        alt50=1;
            	    }
            	    else if ( LA50_0==ID ) {
            	        int LA50_2 = input.LA(2);
            	        if ( LA50_2==36 ) {
            	            int LA50_3 = input.LA(3);
            	            if ( LA50_3==ID ) {
            	                int LA50_8 = input.LA(4);
            	                if ( LA50_8==57 ) {
            	                    alt50=2;
            	                }
            	                else if ( LA50_8==EOL||LA50_8==19||(LA50_8>=28 && LA50_8<=29)||(LA50_8>=47 && LA50_8<=55) ) {
            	                    alt50=1;
            	                }
            	                else {
            	                    NoViableAltException nvae =
            	                        new NoViableAltException("737:39: ( constraint[constraints] | predicate[constraints] )", 50, 8, input);

            	                    throw nvae;
            	                }
            	            }
            	            else if ( LA50_3==EOL||LA50_3==19 ) {
            	                alt50=1;
            	            }
            	            else {
            	                NoViableAltException nvae =
            	                    new NoViableAltException("737:39: ( constraint[constraints] | predicate[constraints] )", 50, 3, input);

            	                throw nvae;
            	            }
            	        }
            	        else if ( LA50_2==EOL||LA50_2==19||(LA50_2>=28 && LA50_2<=29)||(LA50_2>=47 && LA50_2<=55) ) {
            	            alt50=1;
            	        }
            	        else {
            	            NoViableAltException nvae =
            	                new NoViableAltException("737:39: ( constraint[constraints] | predicate[constraints] )", 50, 2, input);

            	            throw nvae;
            	        }
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("737:39: ( constraint[constraints] | predicate[constraints] )", 50, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt50) {
            	        case 1 :
            	            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:737:40: constraint[constraints]
            	            {
            	            following.push(FOLLOW_constraint_in_constraints1733);
            	            constraint(constraints);
            	            following.pop();


            	            }
            	            break;
            	        case 2 :
            	            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:737:64: predicate[constraints]
            	            {
            	            following.push(FOLLOW_predicate_in_constraints1736);
            	            predicate(constraints);
            	            following.pop();


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);

            following.push(FOLLOW_opt_eol_in_constraints1744);
            opt_eol();
            following.pop();


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return constraints;
    }
    // $ANTLR end constraints


    // $ANTLR start constraint
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:741:1: constraint[List constraints] : opt_eol (fb= ID opt_eol ':' opt_eol )? f= ID opt_eol (op= ('=='|'>'|'>='|'<'|'<='|'!='|'contains'|'matches'|'excludes') opt_eol (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint ) )? opt_eol ;
    public void constraint(List constraints) throws RecognitionException {   
        Token fb=null;
        Token f=null;
        Token op=null;
        Token bvc=null;
        String lc = null;

        String rvc = null;


        
        		PatternDescr d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:745:17: ( opt_eol (fb= ID opt_eol ':' opt_eol )? f= ID opt_eol (op= ('=='|'>'|'>='|'<'|'<='|'!='|'contains'|'matches'|'excludes') opt_eol (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint ) )? opt_eol )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:745:17: opt_eol (fb= ID opt_eol ':' opt_eol )? f= ID opt_eol (op= ('=='|'>'|'>='|'<'|'<='|'!='|'contains'|'matches'|'excludes') opt_eol (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint ) )? opt_eol
            {
            following.push(FOLLOW_opt_eol_in_constraint1763);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:746:17: (fb= ID opt_eol ':' opt_eol )?
            int alt52=2;
            alt52 = dfa52.predict(input); 
            switch (alt52) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:746:19: fb= ID opt_eol ':' opt_eol
                    {
                    fb=(Token)input.LT(1);
                    match(input,ID,FOLLOW_ID_in_constraint1771); 
                    following.push(FOLLOW_opt_eol_in_constraint1773);
                    opt_eol();
                    following.pop();

                    match(input,36,FOLLOW_36_in_constraint1775); 
                    following.push(FOLLOW_opt_eol_in_constraint1777);
                    opt_eol();
                    following.pop();


                    }
                    break;

            }

            f=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_constraint1787); 
            
            			if ( fb != null ) {
            				//System.err.println( "fb: " + fb.getText() );
            				//System.err.println( " f: " + f.getText() );
            				d = new FieldBindingDescr( f.getText(), fb.getText() );
            				//System.err.println( "fbd: " + d );
            				
            				d.setLocation( offset(f.getLine()), f.getCharPositionInLine() );
            				constraints.add( d );
            			} 
            		
            following.push(FOLLOW_opt_eol_in_constraint1797);
            opt_eol();
            following.pop();

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:759:33: (op= ('=='|'>'|'>='|'<'|'<='|'!='|'contains'|'matches'|'excludes') opt_eol (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint ) )?
            int alt54=2;
            int LA54_0 = input.LA(1);
            if ( (LA54_0>=47 && LA54_0<=55) ) {
                alt54=1;
            }
            else if ( LA54_0==EOL||LA54_0==19||(LA54_0>=28 && LA54_0<=29) ) {
                alt54=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("759:33: (op= (\'==\'|\'>\'|\'>=\'|\'<\'|\'<=\'|\'!=\'|\'contains\'|\'matches\'|\'excludes\') opt_eol (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint ) )?", 54, 0, input);

                throw nvae;
            }
            switch (alt54) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:759:41: op= ('=='|'>'|'>='|'<'|'<='|'!='|'contains'|'matches'|'excludes') opt_eol (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint )
                    {
                    op=(Token)input.LT(1);
                    if ( (input.LA(1)>=47 && input.LA(1)<=55) ) {
                        input.consume();
                        errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recoverFromMismatchedSet(input,mse,FOLLOW_set_in_constraint1805);    throw mse;
                    }

                    following.push(FOLLOW_opt_eol_in_constraint1892);
                    opt_eol();
                    following.pop();

                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:770:41: (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint )
                    int alt53=4;
                    switch ( input.LA(1) ) {
                    case ID:
                        int LA53_1 = input.LA(2);
                        if ( LA53_1==22 ) {
                            alt53=2;
                        }
                        else if ( LA53_1==EOL||LA53_1==19||(LA53_1>=28 && LA53_1<=29) ) {
                            alt53=1;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("770:41: (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint )", 53, 1, input);

                            throw nvae;
                        }
                        break;
                    case INT:
                    case BOOL:
                    case STRING:
                    case FLOAT:
                    case 56:
                        alt53=3;
                        break;
                    case 27:
                        alt53=4;
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("770:41: (bvc= ID | lc= enum_constraint | lc= literal_constraint | rvc= retval_constraint )", 53, 0, input);

                        throw nvae;
                    }

                    switch (alt53) {
                        case 1 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:770:49: bvc= ID
                            {
                            bvc=(Token)input.LT(1);
                            match(input,ID,FOLLOW_ID_in_constraint1910); 
                            
                            							d = new BoundVariableDescr( f.getText(), op.getText(), bvc.getText() );
                            							d.setLocation( offset(f.getLine()), f.getCharPositionInLine() );
                            							constraints.add( d );
                            						

                            }
                            break;
                        case 2 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:777:49: lc= enum_constraint
                            {
                            following.push(FOLLOW_enum_constraint_in_constraint1935);
                            lc=enum_constraint();
                            following.pop();

                             
                            							d = new LiteralDescr( f.getText(), op.getText(), lc, true ); 
                            							d.setLocation( offset(f.getLine()), f.getCharPositionInLine() );
                            							constraints.add( d );
                            						

                            }
                            break;
                        case 3 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:784:49: lc= literal_constraint
                            {
                            following.push(FOLLOW_literal_constraint_in_constraint1967);
                            lc=literal_constraint();
                            following.pop();

                             
                            							d = new LiteralDescr( f.getText(), op.getText(), lc ); 
                            							d.setLocation( offset(f.getLine()), f.getCharPositionInLine() );
                            							constraints.add( d );
                            						

                            }
                            break;
                        case 4 :
                            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:790:49: rvc= retval_constraint
                            {
                            following.push(FOLLOW_retval_constraint_in_constraint1987);
                            rvc=retval_constraint();
                            following.pop();

                             
                            							d = new ReturnValueDescr( f.getText(), op.getText(), rvc ); 
                            							d.setLocation( offset(f.getLine()), f.getCharPositionInLine() );
                            							constraints.add( d );
                            						

                            }
                            break;

                    }


                    }
                    break;

            }

            following.push(FOLLOW_opt_eol_in_constraint2020);
            opt_eol();
            following.pop();


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end constraint


    // $ANTLR start literal_constraint
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:801:1: literal_constraint returns [String text] : (t= STRING | t= INT | t= FLOAT | t= BOOL | t= 'null' ) ;
    public String literal_constraint() throws RecognitionException {   
        String text;
        Token t=null;

        
        		text = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:805:17: ( (t= STRING | t= INT | t= FLOAT | t= BOOL | t= 'null' ) )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:805:17: (t= STRING | t= INT | t= FLOAT | t= BOOL | t= 'null' )
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:805:17: (t= STRING | t= INT | t= FLOAT | t= BOOL | t= 'null' )
            int alt55=5;
            switch ( input.LA(1) ) {
            case STRING:
                alt55=1;
                break;
            case INT:
                alt55=2;
                break;
            case FLOAT:
                alt55=3;
                break;
            case BOOL:
                alt55=4;
                break;
            case 56:
                alt55=5;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("805:17: (t= STRING | t= INT | t= FLOAT | t= BOOL | t= \'null\' )", 55, 0, input);

                throw nvae;
            }

            switch (alt55) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:805:25: t= STRING
                    {
                    t=(Token)input.LT(1);
                    match(input,STRING,FOLLOW_STRING_in_literal_constraint2047); 
                     text = getString( t ); 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:806:25: t= INT
                    {
                    t=(Token)input.LT(1);
                    match(input,INT,FOLLOW_INT_in_literal_constraint2058); 
                     text = t.getText(); 

                    }
                    break;
                case 3 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:807:25: t= FLOAT
                    {
                    t=(Token)input.LT(1);
                    match(input,FLOAT,FOLLOW_FLOAT_in_literal_constraint2071); 
                     text = t.getText(); 

                    }
                    break;
                case 4 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:808:25: t= BOOL
                    {
                    t=(Token)input.LT(1);
                    match(input,BOOL,FOLLOW_BOOL_in_literal_constraint2082); 
                     text = t.getText(); 

                    }
                    break;
                case 5 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:809:25: t= 'null'
                    {
                    t=(Token)input.LT(1);
                    match(input,56,FOLLOW_56_in_literal_constraint2094); 
                     text = null; 

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return text;
    }
    // $ANTLR end literal_constraint


    // $ANTLR start enum_constraint
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:813:1: enum_constraint returns [String text] : (cls= ID '.' en= ID ) ;
    public String enum_constraint() throws RecognitionException {   
        String text;
        Token cls=null;
        Token en=null;

        
        		text = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:817:17: ( (cls= ID '.' en= ID ) )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:817:17: (cls= ID '.' en= ID )
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:817:17: (cls= ID '.' en= ID )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:817:18: cls= ID '.' en= ID
            {
            cls=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_enum_constraint2125); 
            match(input,22,FOLLOW_22_in_enum_constraint2127); 
            en=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_enum_constraint2131); 

            }

             text = cls.getText() + "." + en.getText(); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return text;
    }
    // $ANTLR end enum_constraint


    // $ANTLR start retval_constraint
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:820:1: retval_constraint returns [String text] : '(' c= paren_chunk ')' ;
    public String retval_constraint() throws RecognitionException {   
        String text;
        String c = null;


        
        		text = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:825:17: ( '(' c= paren_chunk ')' )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:825:17: '(' c= paren_chunk ')'
            {
            match(input,27,FOLLOW_27_in_retval_constraint2160); 
            following.push(FOLLOW_paren_chunk_in_retval_constraint2165);
            c=paren_chunk();
            following.pop();

            match(input,29,FOLLOW_29_in_retval_constraint2168); 
             text = c; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return text;
    }
    // $ANTLR end retval_constraint


    // $ANTLR start predicate
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:828:1: predicate[List constraints] : decl= ID ':' field= ID '->' '(' text= paren_chunk ')' ;
    public void predicate(List constraints) throws RecognitionException {   
        Token decl=null;
        Token field=null;
        String text = null;


        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:830:17: (decl= ID ':' field= ID '->' '(' text= paren_chunk ')' )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:830:17: decl= ID ':' field= ID '->' '(' text= paren_chunk ')'
            {
            decl=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_predicate2186); 
            match(input,36,FOLLOW_36_in_predicate2188); 
            field=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_predicate2192); 
            match(input,57,FOLLOW_57_in_predicate2194); 
            match(input,27,FOLLOW_27_in_predicate2196); 
            following.push(FOLLOW_paren_chunk_in_predicate2200);
            text=paren_chunk();
            following.pop();

            match(input,29,FOLLOW_29_in_predicate2202); 
            
            			PredicateDescr d = new PredicateDescr(field.getText(), decl.getText(), text );
            			constraints.add( d );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end predicate


    // $ANTLR start paren_chunk
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:837:1: paren_chunk returns [String text] : ( options {greedy=false; } : '(' c= paren_chunk ')' | any= . )* ;
    public String paren_chunk() throws RecognitionException {   
        String text;
        Token any=null;
        String c = null;


        
        		text = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:843:18: ( ( options {greedy=false; } : '(' c= paren_chunk ')' | any= . )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:843:18: ( options {greedy=false; } : '(' c= paren_chunk ')' | any= . )*
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:843:18: ( options {greedy=false; } : '(' c= paren_chunk ')' | any= . )*
            loop56:
            do {
                int alt56=3;
                switch ( input.LA(1) ) {
                case EOL:
                    alt56=3;
                    break;
                case 29:
                    alt56=3;
                    break;
                case 27:
                    alt56=1;
                    break;
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 28:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    alt56=2;
                    break;

                }

                switch (alt56) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:844:25: '(' c= paren_chunk ')'
            	    {
            	    match(input,27,FOLLOW_27_in_paren_chunk2248); 
            	    following.push(FOLLOW_paren_chunk_in_paren_chunk2252);
            	    c=paren_chunk();
            	    following.pop();

            	    match(input,29,FOLLOW_29_in_paren_chunk2254); 
            	    
            	    				if ( c == null ) {
            	    					c = "";
            	    				}
            	    				if ( text == null ) {
            	    					text = "( " + c + " )";
            	    				} else {
            	    					text = text + " ( " + c + " )";
            	    				}
            	    			

            	    }
            	    break;
            	case 2 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:855:19: any= .
            	    {
            	    any=(Token)input.LT(1);
            	    matchAny(input); 
            	    
            	    				if ( text == null ) {
            	    					text = any.getText();
            	    				} else {
            	    					text = text + " " + any.getText(); 
            	    				} 
            	    			

            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return text;
    }
    // $ANTLR end paren_chunk


    // $ANTLR start paren_chunk2
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:867:1: paren_chunk2 returns [String text] : ( options {greedy=false; } : '(' c= paren_chunk2 ')' | any= . )* ;
    public String paren_chunk2() throws RecognitionException {   
        String text;
        Token any=null;
        String c = null;


        
        		text = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:873:18: ( ( options {greedy=false; } : '(' c= paren_chunk2 ')' | any= . )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:873:18: ( options {greedy=false; } : '(' c= paren_chunk2 ')' | any= . )*
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:873:18: ( options {greedy=false; } : '(' c= paren_chunk2 ')' | any= . )*
            loop57:
            do {
                int alt57=3;
                switch ( input.LA(1) ) {
                case 29:
                    alt57=3;
                    break;
                case 27:
                    alt57=1;
                    break;
                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 28:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    alt57=2;
                    break;

                }

                switch (alt57) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:874:25: '(' c= paren_chunk2 ')'
            	    {
            	    match(input,27,FOLLOW_27_in_paren_chunk22325); 
            	    following.push(FOLLOW_paren_chunk2_in_paren_chunk22329);
            	    c=paren_chunk2();
            	    following.pop();

            	    match(input,29,FOLLOW_29_in_paren_chunk22331); 
            	    
            	    				if ( c == null ) {
            	    					c = "";
            	    				}
            	    				if ( text == null ) {
            	    					text = "( " + c + " )";
            	    				} else {
            	    					text = text + " ( " + c + " )";
            	    				}
            	    			

            	    }
            	    break;
            	case 2 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:885:19: any= .
            	    {
            	    any=(Token)input.LT(1);
            	    matchAny(input); 
            	    
            	    				if ( text == null ) {
            	    					text = any.getText();
            	    				} else {
            	    					text = text + " " + any.getText(); 
            	    				} 
            	    			

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return text;
    }
    // $ANTLR end paren_chunk2


    // $ANTLR start curly_chunk
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:896:1: curly_chunk returns [String text] : ( options {greedy=false; } : '{' c= curly_chunk '}' | any= . )* ;
    public String curly_chunk() throws RecognitionException {   
        String text;
        Token any=null;
        String c = null;


        
        		text = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:902:17: ( ( options {greedy=false; } : '{' c= curly_chunk '}' | any= . )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:902:17: ( options {greedy=false; } : '{' c= curly_chunk '}' | any= . )*
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:902:17: ( options {greedy=false; } : '{' c= curly_chunk '}' | any= . )*
            loop58:
            do {
                int alt58=3;
                switch ( input.LA(1) ) {
                case 31:
                    alt58=3;
                    break;
                case 30:
                    alt58=1;
                    break;
                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    alt58=2;
                    break;

                }

                switch (alt58) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:903:25: '{' c= curly_chunk '}'
            	    {
            	    match(input,30,FOLLOW_30_in_curly_chunk2400); 
            	    following.push(FOLLOW_curly_chunk_in_curly_chunk2404);
            	    c=curly_chunk();
            	    following.pop();

            	    match(input,31,FOLLOW_31_in_curly_chunk2406); 
            	    
            	    				//System.err.println( "chunk [" + c + "]" );
            	    				if ( c == null ) {
            	    					c = "";
            	    				}
            	    				if ( text == null ) {
            	    					text = "{ " + c + " }";
            	    				} else {
            	    					text = text + " { " + c + " }";
            	    				}
            	    			

            	    }
            	    break;
            	case 2 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:915:19: any= .
            	    {
            	    any=(Token)input.LT(1);
            	    matchAny(input); 
            	    
            	    				//System.err.println( "any [" + any.getText() + "]" );
            	    				if ( text == null ) {
            	    					text = any.getText();
            	    				} else {
            	    					text = text + " " + any.getText(); 
            	    				} 
            	    			

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return text;
    }
    // $ANTLR end curly_chunk


    // $ANTLR start lhs_or
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:927:1: lhs_or returns [PatternDescr d] : left= lhs_and ( ('or'|'||') opt_eol right= lhs_and )* ;
    public PatternDescr lhs_or() throws RecognitionException {   
        PatternDescr d;
        PatternDescr left = null;

        PatternDescr right = null;


        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:932:17: (left= lhs_and ( ('or'|'||') opt_eol right= lhs_and )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:932:17: left= lhs_and ( ('or'|'||') opt_eol right= lhs_and )*
            {
             OrDescr or = null; 
            following.push(FOLLOW_lhs_and_in_lhs_or2464);
            left=lhs_and();
            following.pop();

            d = left; 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:934:17: ( ('or'|'||') opt_eol right= lhs_and )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);
                if ( (LA59_0>=45 && LA59_0<=46) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:934:19: ('or'|'||') opt_eol right= lhs_and
            	    {
            	    if ( (input.LA(1)>=45 && input.LA(1)<=46) ) {
            	        input.consume();
            	        errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recoverFromMismatchedSet(input,mse,FOLLOW_set_in_lhs_or2473);    throw mse;
            	    }

            	    following.push(FOLLOW_opt_eol_in_lhs_or2478);
            	    opt_eol();
            	    following.pop();

            	    following.push(FOLLOW_lhs_and_in_lhs_or2485);
            	    right=lhs_and();
            	    following.pop();

            	    
            	    				if ( or == null ) {
            	    					or = new OrDescr();
            	    					or.addDescr( left );
            	    					d = or;
            	    				}
            	    				
            	    				or.addDescr( right );
            	    			

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_or


    // $ANTLR start lhs_and
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:948:1: lhs_and returns [PatternDescr d] : left= lhs_unary ( ('and'|'&&') opt_eol right= lhs_unary )* ;
    public PatternDescr lhs_and() throws RecognitionException {   
        PatternDescr d;
        PatternDescr left = null;

        PatternDescr right = null;


        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:953:17: (left= lhs_unary ( ('and'|'&&') opt_eol right= lhs_unary )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:953:17: left= lhs_unary ( ('and'|'&&') opt_eol right= lhs_unary )*
            {
             AndDescr and = null; 
            following.push(FOLLOW_lhs_unary_in_lhs_and2525);
            left=lhs_unary();
            following.pop();

             d = left; 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:955:17: ( ('and'|'&&') opt_eol right= lhs_unary )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);
                if ( (LA60_0>=58 && LA60_0<=59) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:955:19: ('and'|'&&') opt_eol right= lhs_unary
            	    {
            	    if ( (input.LA(1)>=58 && input.LA(1)<=59) ) {
            	        input.consume();
            	        errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recoverFromMismatchedSet(input,mse,FOLLOW_set_in_lhs_and2534);    throw mse;
            	    }

            	    following.push(FOLLOW_opt_eol_in_lhs_and2539);
            	    opt_eol();
            	    following.pop();

            	    following.push(FOLLOW_lhs_unary_in_lhs_and2546);
            	    right=lhs_unary();
            	    following.pop();

            	    
            	    				if ( and == null ) {
            	    					and = new AndDescr();
            	    					and.addDescr( left );
            	    					d = and;
            	    				}
            	    				
            	    				and.addDescr( right );
            	    			

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_and


    // $ANTLR start lhs_unary
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:969:1: lhs_unary returns [PatternDescr d] : (u= lhs_exist | u= lhs_not | u= lhs_eval | u= lhs_column | '(' opt_eol u= lhs opt_eol ')' ) ;
    public PatternDescr lhs_unary() throws RecognitionException {   
        PatternDescr d;
        PatternDescr u = null;


        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:973:17: ( (u= lhs_exist | u= lhs_not | u= lhs_eval | u= lhs_column | '(' opt_eol u= lhs opt_eol ')' ) )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:973:17: (u= lhs_exist | u= lhs_not | u= lhs_eval | u= lhs_column | '(' opt_eol u= lhs opt_eol ')' )
            {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:973:17: (u= lhs_exist | u= lhs_not | u= lhs_eval | u= lhs_column | '(' opt_eol u= lhs opt_eol ')' )
            int alt61=5;
            switch ( input.LA(1) ) {
            case 60:
                alt61=1;
                break;
            case 61:
                alt61=2;
                break;
            case 62:
                alt61=3;
                break;
            case ID:
                alt61=4;
                break;
            case 27:
                alt61=5;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("973:17: (u= lhs_exist | u= lhs_not | u= lhs_eval | u= lhs_column | \'(\' opt_eol u= lhs opt_eol \')\' )", 61, 0, input);

                throw nvae;
            }

            switch (alt61) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:973:25: u= lhs_exist
                    {
                    following.push(FOLLOW_lhs_exist_in_lhs_unary2584);
                    u=lhs_exist();
                    following.pop();


                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:974:25: u= lhs_not
                    {
                    following.push(FOLLOW_lhs_not_in_lhs_unary2592);
                    u=lhs_not();
                    following.pop();


                    }
                    break;
                case 3 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:975:25: u= lhs_eval
                    {
                    following.push(FOLLOW_lhs_eval_in_lhs_unary2600);
                    u=lhs_eval();
                    following.pop();


                    }
                    break;
                case 4 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:976:25: u= lhs_column
                    {
                    following.push(FOLLOW_lhs_column_in_lhs_unary2608);
                    u=lhs_column();
                    following.pop();


                    }
                    break;
                case 5 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:977:25: '(' opt_eol u= lhs opt_eol ')'
                    {
                    match(input,27,FOLLOW_27_in_lhs_unary2614); 
                    following.push(FOLLOW_opt_eol_in_lhs_unary2616);
                    opt_eol();
                    following.pop();

                    following.push(FOLLOW_lhs_in_lhs_unary2620);
                    u=lhs();
                    following.pop();

                    following.push(FOLLOW_opt_eol_in_lhs_unary2622);
                    opt_eol();
                    following.pop();

                    match(input,29,FOLLOW_29_in_lhs_unary2624); 

                    }
                    break;

            }

             d = u; 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_unary


    // $ANTLR start lhs_exist
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:981:1: lhs_exist returns [PatternDescr d] : loc= 'exists' ( '(' column= lhs_column ')' | column= lhs_column ) ;
    public PatternDescr lhs_exist() throws RecognitionException {   
        PatternDescr d;
        Token loc=null;
        PatternDescr column = null;


        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:985:17: (loc= 'exists' ( '(' column= lhs_column ')' | column= lhs_column ) )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:985:17: loc= 'exists' ( '(' column= lhs_column ')' | column= lhs_column )
            {
            loc=(Token)input.LT(1);
            match(input,60,FOLLOW_60_in_lhs_exist2654); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:985:30: ( '(' column= lhs_column ')' | column= lhs_column )
            int alt62=2;
            int LA62_0 = input.LA(1);
            if ( LA62_0==27 ) {
                alt62=1;
            }
            else if ( LA62_0==ID ) {
                alt62=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("985:30: ( \'(\' column= lhs_column \')\' | column= lhs_column )", 62, 0, input);

                throw nvae;
            }
            switch (alt62) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:985:31: '(' column= lhs_column ')'
                    {
                    match(input,27,FOLLOW_27_in_lhs_exist2657); 
                    following.push(FOLLOW_lhs_column_in_lhs_exist2661);
                    column=lhs_column();
                    following.pop();

                    match(input,29,FOLLOW_29_in_lhs_exist2663); 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:985:59: column= lhs_column
                    {
                    following.push(FOLLOW_lhs_column_in_lhs_exist2669);
                    column=lhs_column();
                    following.pop();


                    }
                    break;

            }

             
            			d = new ExistsDescr( (ColumnDescr) column ); 
            			d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_exist


    // $ANTLR start lhs_not
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:992:1: lhs_not returns [NotDescr d] : loc= 'not' ( '(' column= lhs_column ')' | column= lhs_column ) ;
    public NotDescr lhs_not() throws RecognitionException {   
        NotDescr d;
        Token loc=null;
        PatternDescr column = null;


        
        		d = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:996:17: (loc= 'not' ( '(' column= lhs_column ')' | column= lhs_column ) )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:996:17: loc= 'not' ( '(' column= lhs_column ')' | column= lhs_column )
            {
            loc=(Token)input.LT(1);
            match(input,61,FOLLOW_61_in_lhs_not2699); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:996:27: ( '(' column= lhs_column ')' | column= lhs_column )
            int alt63=2;
            int LA63_0 = input.LA(1);
            if ( LA63_0==27 ) {
                alt63=1;
            }
            else if ( LA63_0==ID ) {
                alt63=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("996:27: ( \'(\' column= lhs_column \')\' | column= lhs_column )", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:996:28: '(' column= lhs_column ')'
                    {
                    match(input,27,FOLLOW_27_in_lhs_not2702); 
                    following.push(FOLLOW_lhs_column_in_lhs_not2706);
                    column=lhs_column();
                    following.pop();

                    match(input,29,FOLLOW_29_in_lhs_not2709); 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:996:57: column= lhs_column
                    {
                    following.push(FOLLOW_lhs_column_in_lhs_not2715);
                    column=lhs_column();
                    following.pop();


                    }
                    break;

            }

            
            			d = new NotDescr( (ColumnDescr) column ); 
            			d.setLocation( offset(loc.getLine()), loc.getCharPositionInLine() );
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_not


    // $ANTLR start lhs_eval
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1003:1: lhs_eval returns [PatternDescr d] : 'eval' loc= '(' c= paren_chunk2 ')' ;
    public PatternDescr lhs_eval() throws RecognitionException {   
        PatternDescr d;
        Token loc=null;
        String c = null;


        
        		d = null;
        		String text = "";
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1008:17: ( 'eval' loc= '(' c= paren_chunk2 ')' )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1008:17: 'eval' loc= '(' c= paren_chunk2 ')'
            {
            match(input,62,FOLLOW_62_in_lhs_eval2741); 
            loc=(Token)input.LT(1);
            match(input,27,FOLLOW_27_in_lhs_eval2745); 
            following.push(FOLLOW_paren_chunk2_in_lhs_eval2753);
            c=paren_chunk2();
            following.pop();

            match(input,29,FOLLOW_29_in_lhs_eval2757); 
             
            			checkTrailingSemicolon( c, offset(loc.getLine()) );
            			d = new EvalDescr( c ); 
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return d;
    }
    // $ANTLR end lhs_eval


    // $ANTLR start dotted_name
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1017:1: dotted_name returns [String name] : id= ID ( '.' id= ID )* ( '[' ']' )* ;
    public String dotted_name() throws RecognitionException {   
        String name;
        Token id=null;

        
        		name = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1022:17: (id= ID ( '.' id= ID )* ( '[' ']' )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1022:17: id= ID ( '.' id= ID )* ( '[' ']' )*
            {
            id=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_dotted_name2789); 
             name=id.getText(); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1022:46: ( '.' id= ID )*
            loop64:
            do {
                int alt64=2;
                int LA64_0 = input.LA(1);
                if ( LA64_0==22 ) {
                    alt64=1;
                }


                switch (alt64) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1022:48: '.' id= ID
            	    {
            	    match(input,22,FOLLOW_22_in_dotted_name2795); 
            	    id=(Token)input.LT(1);
            	    match(input,ID,FOLLOW_ID_in_dotted_name2799); 
            	     name = name + "." + id.getText(); 

            	    }
            	    break;

            	default :
            	    break loop64;
                }
            } while (true);

            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1022:99: ( '[' ']' )*
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);
                if ( LA65_0==63 ) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1022:101: '[' ']'
            	    {
            	    match(input,63,FOLLOW_63_in_dotted_name2808); 
            	    match(input,64,FOLLOW_64_in_dotted_name2810); 
            	     name = name + "[]";

            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return name;
    }
    // $ANTLR end dotted_name


    // $ANTLR start argument
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1025:1: argument returns [String name] : id= ID ( '[' ']' )* ;
    public String argument() throws RecognitionException {   
        String name;
        Token id=null;

        
        		name = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1030:17: (id= ID ( '[' ']' )* )
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1030:17: id= ID ( '[' ']' )*
            {
            id=(Token)input.LT(1);
            match(input,ID,FOLLOW_ID_in_argument2840); 
             name=id.getText(); 
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1030:46: ( '[' ']' )*
            loop66:
            do {
                int alt66=2;
                int LA66_0 = input.LA(1);
                if ( LA66_0==63 ) {
                    alt66=1;
                }


                switch (alt66) {
            	case 1 :
            	    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1030:48: '[' ']'
            	    {
            	    match(input,63,FOLLOW_63_in_argument2846); 
            	    match(input,64,FOLLOW_64_in_argument2848); 
            	     name = name + "[]";

            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return name;
    }
    // $ANTLR end argument


    // $ANTLR start word
    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1034:1: word returns [String word] : (id= ID | 'import' | 'use' | 'rule' | 'query' | 'salience' | 'no-loop' | 'when' | 'then' | 'end' | str= STRING );
    public String word() throws RecognitionException {   
        String word;
        Token id=null;
        Token str=null;

        
        		word = null;
        	
        try {
            // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1038:17: (id= ID | 'import' | 'use' | 'rule' | 'query' | 'salience' | 'no-loop' | 'when' | 'then' | 'end' | str= STRING )
            int alt67=11;
            switch ( input.LA(1) ) {
            case ID:
                alt67=1;
                break;
            case 21:
                alt67=2;
                break;
            case 65:
                alt67=3;
                break;
            case 34:
                alt67=4;
                break;
            case 32:
                alt67=5;
                break;
            case 39:
                alt67=6;
                break;
            case 40:
                alt67=7;
                break;
            case 35:
                alt67=8;
                break;
            case 37:
                alt67=9;
                break;
            case 33:
                alt67=10;
                break;
            case STRING:
                alt67=11;
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("1034:1: word returns [String word] : (id= ID | \'import\' | \'use\' | \'rule\' | \'query\' | \'salience\' | \'no-loop\' | \'when\' | \'then\' | \'end\' | str= STRING );", 67, 0, input);

                throw nvae;
            }

            switch (alt67) {
                case 1 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1038:17: id= ID
                    {
                    id=(Token)input.LT(1);
                    match(input,ID,FOLLOW_ID_in_word2876); 
                     word=id.getText(); 

                    }
                    break;
                case 2 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1039:17: 'import'
                    {
                    match(input,21,FOLLOW_21_in_word2888); 
                     word="import"; 

                    }
                    break;
                case 3 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1040:17: 'use'
                    {
                    match(input,65,FOLLOW_65_in_word2897); 
                     word="use"; 

                    }
                    break;
                case 4 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1041:17: 'rule'
                    {
                    match(input,34,FOLLOW_34_in_word2909); 
                     word="rule"; 

                    }
                    break;
                case 5 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1042:17: 'query'
                    {
                    match(input,32,FOLLOW_32_in_word2920); 
                     word="query"; 

                    }
                    break;
                case 6 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1043:17: 'salience'
                    {
                    match(input,39,FOLLOW_39_in_word2930); 
                     word="salience"; 

                    }
                    break;
                case 7 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1044:17: 'no-loop'
                    {
                    match(input,40,FOLLOW_40_in_word2938); 
                     word="no-loop"; 

                    }
                    break;
                case 8 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1045:17: 'when'
                    {
                    match(input,35,FOLLOW_35_in_word2946); 
                     word="when"; 

                    }
                    break;
                case 9 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1046:17: 'then'
                    {
                    match(input,37,FOLLOW_37_in_word2957); 
                     word="then"; 

                    }
                    break;
                case 10 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1047:17: 'end'
                    {
                    match(input,33,FOLLOW_33_in_word2968); 
                     word="end"; 

                    }
                    break;
                case 11 :
                    // D:\workspace\jboss\jbrules-3.0.x\drools-compiler\src\main\resources\org\drools\lang\drl.g:1048:17: str= STRING
                    {
                    str=(Token)input.LT(1);
                    match(input,STRING,FOLLOW_STRING_in_word2982); 
                     word=getString(str);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return word;
    }
    // $ANTLR end word


    protected DFA2 dfa2 = new DFA2();protected DFA4 dfa4 = new DFA4();protected DFA12 dfa12 = new DFA12();protected DFA13 dfa13 = new DFA13();protected DFA14 dfa14 = new DFA14();protected DFA45 dfa45 = new DFA45();protected DFA48 dfa48 = new DFA48();protected DFA51 dfa51 = new DFA51();protected DFA52 dfa52 = new DFA52();
    class DFA2 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s1 = new DFA.State() {{alt=4;}};
        DFA.State s3 = new DFA.State() {{alt=1;}};
        DFA.State s4 = new DFA.State() {{alt=2;}};
        DFA.State s2 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 34:
                    return s3;

                case EOL:
                case 19:
                    return s2;

                case 32:
                    return s4;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 2, input);

                    throw nvae;        }
            }
        };
        DFA.State s5 = new DFA.State() {{alt=3;}};
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case -1:
                    return s1;

                case EOL:
                case 19:
                    return s2;

                case 34:
                    return s3;

                case 32:
                    return s4;

                case 21:
                case 25:
                case 26:
                    return s5;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 0, input);

                    throw nvae;        }
            }
        };

    }class DFA4 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s1 = new DFA.State() {{alt=3;}};
        DFA.State s10 = new DFA.State() {{alt=1;}};
        DFA.State s9 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_9 = input.LA(1);
                if ( LA4_9==ID ) {return s10;}
                if ( LA4_9==EOL||LA4_9==19 ) {return s9;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 9, input);

                throw nvae;
            }
        };
        DFA.State s4 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_4 = input.LA(1);
                if ( LA4_4==EOL||LA4_4==19 ) {return s9;}
                if ( LA4_4==ID ) {return s10;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 4, input);

                throw nvae;
            }
        };
        DFA.State s18 = new DFA.State() {{alt=1;}};
        DFA.State s27 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_27 = input.LA(1);
                if ( LA4_27==ID ) {return s18;}
                if ( LA4_27==63 ) {return s17;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 27, input);

                throw nvae;
            }
        };
        DFA.State s17 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_17 = input.LA(1);
                if ( LA4_17==64 ) {return s27;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 17, input);

                throw nvae;
            }
        };
        DFA.State s26 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 63:
                    return s17;

                case ID:
                    return s18;

                case 22:
                    return s16;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 26, input);

                    throw nvae;        }
            }
        };
        DFA.State s16 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_16 = input.LA(1);
                if ( LA4_16==ID ) {return s26;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 16, input);

                throw nvae;
            }
        };
        DFA.State s11 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 22:
                    return s16;

                case 63:
                    return s17;

                case ID:
                    return s18;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 11, input);

                    throw nvae;        }
            }
        };
        DFA.State s5 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_5 = input.LA(1);
                if ( LA4_5==ID ) {return s11;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 5, input);

                throw nvae;
            }
        };
        DFA.State s105 = new DFA.State() {{alt=1;}};
        DFA.State s114 = new DFA.State() {{alt=1;}};
        DFA.State s121 = new DFA.State() {{alt=1;}};
        DFA.State s122 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s121;

                case 30:
                    return s120;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s122;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 122, input);

                    throw nvae;        }
            }
        };
        DFA.State s120 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s121;

                case 30:
                    return s120;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s122;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 120, input);

                    throw nvae;        }
            }
        };
        DFA.State s115 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 30:
                    return s120;

                case 31:
                    return s121;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s122;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 115, input);

                    throw nvae;        }
            }
        };
        DFA.State s116 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s114;

                case 30:
                    return s115;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s116;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 116, input);

                    throw nvae;        }
            }
        };
        DFA.State s106 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s114;

                case 30:
                    return s115;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s116;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 106, input);

                    throw nvae;        }
            }
        };
        DFA.State s107 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s105;

                case 30:
                    return s106;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s107;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 107, input);

                    throw nvae;        }
            }
        };
        DFA.State s87 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s105;

                case 30:
                    return s106;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s107;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 87, input);

                    throw nvae;        }
            }
        };
        DFA.State s88 = new DFA.State() {{alt=1;}};
        DFA.State s89 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s88;

                case 30:
                    return s87;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s89;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 89, input);

                    throw nvae;        }
            }
        };
        DFA.State s67 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 30:
                    return s87;

                case 31:
                    return s88;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s89;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 67, input);

                    throw nvae;        }
            }
        };
        DFA.State s68 = new DFA.State() {{alt=1;}};
        DFA.State s69 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 31:
                    return s68;

                case 30:
                    return s67;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s69;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 69, input);

                    throw nvae;        }
            }
        };
        DFA.State s51 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 30:
                    return s67;

                case 31:
                    return s68;

                case EOL:
                case ID:
                case INT:
                case BOOL:
                case STRING:
                case FLOAT:
                case MISC:
                case WS:
                case EscapeSequence:
                case HexDigit:
                case UnicodeEscape:
                case OctalEscape:
                case SH_STYLE_SINGLE_LINE_COMMENT:
                case C_STYLE_SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 65:
                    return s69;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 51, input);

                    throw nvae;        }
            }
        };
        DFA.State s50 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_50 = input.LA(1);
                if ( LA4_50==30 ) {return s51;}
                if ( LA4_50==EOL||LA4_50==19 ) {return s50;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 50, input);

                throw nvae;
            }
        };
        DFA.State s33 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_33 = input.LA(1);
                if ( LA4_33==EOL||LA4_33==19 ) {return s50;}
                if ( LA4_33==30 ) {return s51;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 33, input);

                throw nvae;
            }
        };
        DFA.State s62 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 28:
                    return s46;

                case 29:
                    return s33;

                case EOL:
                case 19:
                    return s62;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 62, input);

                    throw nvae;        }
            }
        };
        DFA.State s83 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s62;

                case 28:
                    return s46;

                case 29:
                    return s33;

                case 63:
                    return s61;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 83, input);

                    throw nvae;        }
            }
        };
        DFA.State s61 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_61 = input.LA(1);
                if ( LA4_61==64 ) {return s83;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 61, input);

                throw nvae;
            }
        };
        DFA.State s98 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 29:
                    return s33;

                case 28:
                    return s46;

                case EOL:
                case 19:
                    return s98;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 98, input);

                    throw nvae;        }
            }
        };
        DFA.State s82 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 63:
                    return s61;

                case EOL:
                case 19:
                    return s98;

                case 29:
                    return s33;

                case 28:
                    return s46;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 82, input);

                    throw nvae;        }
            }
        };
        DFA.State s78 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 29:
                    return s33;

                case 28:
                    return s46;

                case EOL:
                case 19:
                    return s78;

                case ID:
                    return s82;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 78, input);

                    throw nvae;        }
            }
        };
        DFA.State s60 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 63:
                    return s44;

                case EOL:
                case 19:
                    return s78;

                case 29:
                    return s33;

                case 28:
                    return s46;

                case 22:
                    return s16;

                case ID:
                    return s82;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 60, input);

                    throw nvae;        }
            }
        };
        DFA.State s59 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_59 = input.LA(1);
                if ( LA4_59==ID ) {return s60;}
                if ( LA4_59==EOL||LA4_59==19 ) {return s59;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 59, input);

                throw nvae;
            }
        };
        DFA.State s46 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_46 = input.LA(1);
                if ( LA4_46==EOL||LA4_46==19 ) {return s59;}
                if ( LA4_46==ID ) {return s60;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 46, input);

                throw nvae;
            }
        };
        DFA.State s49 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 63:
                    return s61;

                case EOL:
                case 19:
                    return s62;

                case 28:
                    return s46;

                case 29:
                    return s33;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 49, input);

                    throw nvae;        }
            }
        };
        DFA.State s45 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 28:
                    return s46;

                case 29:
                    return s33;

                case EOL:
                case 19:
                    return s45;

                case ID:
                    return s49;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 45, input);

                    throw nvae;        }
            }
        };
        DFA.State s54 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s45;

                case ID:
                    return s49;

                case 63:
                    return s44;

                case 28:
                    return s46;

                case 29:
                    return s33;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 54, input);

                    throw nvae;        }
            }
        };
        DFA.State s44 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_44 = input.LA(1);
                if ( LA4_44==64 ) {return s54;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 44, input);

                throw nvae;
            }
        };
        DFA.State s32 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 63:
                    return s44;

                case EOL:
                case 19:
                    return s45;

                case 28:
                    return s46;

                case 29:
                    return s33;

                case 22:
                    return s16;

                case ID:
                    return s49;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 32, input);

                    throw nvae;        }
            }
        };
        DFA.State s31 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case ID:
                    return s32;

                case EOL:
                case 19:
                    return s31;

                case 29:
                    return s33;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 31, input);

                    throw nvae;        }
            }
        };
        DFA.State s22 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s31;

                case ID:
                    return s32;

                case 29:
                    return s33;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 22, input);

                    throw nvae;        }
            }
        };
        DFA.State s34 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_34 = input.LA(1);
                if ( LA4_34==27 ) {return s22;}
                if ( LA4_34==EOL||LA4_34==19 ) {return s34;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 34, input);

                throw nvae;
            }
        };
        DFA.State s25 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_25 = input.LA(1);
                if ( LA4_25==EOL||LA4_25==19 ) {return s34;}
                if ( LA4_25==27 ) {return s22;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 25, input);

                throw nvae;
            }
        };
        DFA.State s21 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 27:
                    return s22;

                case EOL:
                case 19:
                    return s21;

                case ID:
                    return s25;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 21, input);

                    throw nvae;        }
            }
        };
        DFA.State s13 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s21;

                case 27:
                    return s22;

                case 22:
                    return s16;

                case 63:
                    return s17;

                case ID:
                    return s25;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 13, input);

                    throw nvae;        }
            }
        };
        DFA.State s12 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_12 = input.LA(1);
                if ( LA4_12==ID ) {return s13;}
                if ( LA4_12==EOL||LA4_12==19 ) {return s12;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 12, input);

                throw nvae;
            }
        };
        DFA.State s6 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA4_6 = input.LA(1);
                if ( LA4_6==EOL||LA4_6==19 ) {return s12;}
                if ( LA4_6==ID ) {return s13;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 4, 6, input);

                throw nvae;
            }
        };
        DFA.State s8 = new DFA.State() {{alt=2;}};
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case -1:
                case EOL:
                case 19:
                case 32:
                case 34:
                    return s1;

                case 21:
                    return s4;

                case 25:
                    return s5;

                case 26:
                    return s6;

                case 24:
                    return s8;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 0, input);

                    throw nvae;        }
            }
        };

    }class DFA12 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s5 = new DFA.State() {{alt=1;}};
        DFA.State s2 = new DFA.State() {{alt=2;}};
        DFA.State s3 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case ID:
                    return s5;

                case EOL:
                case 19:
                    return s3;

                case 27:
                    return s2;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 3, input);

                    throw nvae;        }
            }
        };
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s3;

                case 27:
                    return s2;

                case ID:
                case 22:
                case 63:
                    return s5;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA12_0 = input.LA(1);
                if ( LA12_0==ID ) {return s1;}
                if ( LA12_0==EOL||LA12_0==19 ) {return s2;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
        };

    }class DFA13 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s3 = new DFA.State() {{alt=1;}};
        DFA.State s2 = new DFA.State() {{alt=2;}};
        DFA.State s5 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case ID:
                    return s3;

                case EOL:
                case 19:
                    return s5;

                case 28:
                case 29:
                    return s2;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 5, input);

                    throw nvae;        }
            }
        };
        DFA.State s9 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s5;

                case 28:
                case 29:
                    return s2;

                case 63:
                    return s4;

                case ID:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 9, input);

                    throw nvae;        }
            }
        };
        DFA.State s4 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA13_4 = input.LA(1);
                if ( LA13_4==64 ) {return s9;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 13, 4, input);

                throw nvae;
            }
        };
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case ID:
                case 22:
                    return s3;

                case 63:
                    return s4;

                case EOL:
                case 19:
                    return s5;

                case 28:
                case 29:
                    return s2;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA13_0 = input.LA(1);
                if ( LA13_0==ID ) {return s1;}
                if ( LA13_0==EOL||LA13_0==19 ) {return s2;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
        };

    }class DFA14 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s3 = new DFA.State() {{alt=1;}};
        DFA.State s2 = new DFA.State() {{alt=2;}};
        DFA.State s5 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 28:
                case 29:
                    return s2;

                case EOL:
                case 19:
                    return s5;

                case ID:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 14, 5, input);

                    throw nvae;        }
            }
        };
        DFA.State s9 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s5;

                case 28:
                case 29:
                    return s2;

                case 63:
                    return s4;

                case ID:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 14, 9, input);

                    throw nvae;        }
            }
        };
        DFA.State s4 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA14_4 = input.LA(1);
                if ( LA14_4==64 ) {return s9;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 14, 4, input);

                throw nvae;
            }
        };
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case ID:
                case 22:
                    return s3;

                case 63:
                    return s4;

                case EOL:
                case 19:
                    return s5;

                case 28:
                case 29:
                    return s2;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 14, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA14_0 = input.LA(1);
                if ( LA14_0==ID ) {return s1;}
                if ( LA14_0==EOL||LA14_0==19 ) {return s2;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
        };

    }class DFA45 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s2 = new DFA.State() {{alt=2;}};
        DFA.State s6 = new DFA.State() {{alt=1;}};
        DFA.State s4 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 27:
                    return s2;

                case EOL:
                case 19:
                    return s4;

                case 36:
                    return s6;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 45, 4, input);

                    throw nvae;        }
            }
        };
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 22:
                case 27:
                case 63:
                    return s2;

                case EOL:
                case 19:
                    return s4;

                case 36:
                    return s6;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 45, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA45_0 = input.LA(1);
                if ( LA45_0==ID ) {return s1;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }
        };

    }class DFA48 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s2 = new DFA.State() {{alt=1;}};
        DFA.State s3 = new DFA.State() {{alt=2;}};
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s1;

                case ID:
                    return s2;

                case 29:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 48, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s1;

                case ID:
                    return s2;

                case 29:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 48, 0, input);

                    throw nvae;        }
            }
        };

    }class DFA51 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s2 = new DFA.State() {{alt=2;}};
        DFA.State s3 = new DFA.State() {{alt=1;}};
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 29:
                    return s2;

                case EOL:
                case 19:
                    return s1;

                case 28:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s1;

                case 29:
                    return s2;

                case 28:
                    return s3;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 0, input);

                    throw nvae;        }
            }
        };

    }class DFA52 extends DFA {
        public int predict(IntStream input) throws RecognitionException {
            return predict(input, s0);
        }
        DFA.State s3 = new DFA.State() {{alt=1;}};
        DFA.State s4 = new DFA.State() {{alt=2;}};
        DFA.State s2 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case 36:
                    return s3;

                case EOL:
                case 19:
                    return s2;

                case 28:
                case 29:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                    return s4;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 52, 2, input);

                    throw nvae;        }
            }
        };
        DFA.State s1 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                switch ( input.LA(1) ) {
                case EOL:
                case 19:
                    return s2;

                case 36:
                    return s3;

                case 28:
                case 29:
                case 47:
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                    return s4;

                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 52, 1, input);

                    throw nvae;        }
            }
        };
        DFA.State s0 = new DFA.State() {
            public DFA.State transition(IntStream input) throws RecognitionException {
                int LA52_0 = input.LA(1);
                if ( LA52_0==ID ) {return s1;}

                NoViableAltException nvae =
        	    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }
        };

    }


    public static final BitSet FOLLOW_set_in_opt_eol41 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_compilation_unit57 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_prolog_in_compilation_unit61 = new BitSet(new long[]{0x0000000006280012L});
    public static final BitSet FOLLOW_rule_in_compilation_unit70 = new BitSet(new long[]{0x0000000006280012L});
    public static final BitSet FOLLOW_query_in_compilation_unit83 = new BitSet(new long[]{0x0000000006280012L});
    public static final BitSet FOLLOW_extra_statement_in_compilation_unit91 = new BitSet(new long[]{0x0000000006280012L});
    public static final BitSet FOLLOW_opt_eol_in_prolog115 = new BitSet(new long[]{0x0000000007380012L});
    public static final BitSet FOLLOW_package_statement_in_prolog123 = new BitSet(new long[]{0x0000000007280012L});
    public static final BitSet FOLLOW_extra_statement_in_prolog138 = new BitSet(new long[]{0x0000000007280012L});
    public static final BitSet FOLLOW_expander_in_prolog144 = new BitSet(new long[]{0x0000000007280012L});
    public static final BitSet FOLLOW_opt_eol_in_prolog156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_package_statement180 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_package_statement182 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_dotted_name_in_package_statement186 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_package_statement188 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_package_statement191 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_import_statement207 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_import_statement209 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_import_name_in_import_statement213 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_import_statement215 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_import_statement218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_import_name249 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_22_in_import_name255 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_import_name259 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_23_in_import_name269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_expander289 = new BitSet(new long[]{0x0000000000080032L});
    public static final BitSet FOLLOW_dotted_name_in_expander294 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_expander298 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_expander301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_global325 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_dotted_name_in_global329 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_global333 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_global335 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_global338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_function362 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function364 = new BitSet(new long[]{0x0000000000080032L});
    public static final BitSet FOLLOW_dotted_name_in_function369 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function373 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_function377 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function379 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_function388 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function390 = new BitSet(new long[]{0x0000000020080032L});
    public static final BitSet FOLLOW_dotted_name_in_function400 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function404 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_argument_in_function408 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function410 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_28_in_function424 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function426 = new BitSet(new long[]{0x0000000000080032L});
    public static final BitSet FOLLOW_dotted_name_in_function431 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function435 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_argument_in_function439 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function441 = new BitSet(new long[]{0x0000000030000000L});
    public static final BitSet FOLLOW_29_in_function466 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function470 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_function474 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_curly_chunk_in_function481 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_function490 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_function498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_opt_eol_in_query522 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_32_in_query528 = new BitSet(new long[]{0x000001AF00200120L,0x0000000000000002L});
    public static final BitSet FOLLOW_word_in_query532 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_query534 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_expander_lhs_block_in_query550 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_normal_lhs_block_in_query558 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_query573 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_query575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_opt_eol_in_rule598 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_34_in_rule604 = new BitSet(new long[]{0x000001AF00200120L,0x0000000000000002L});
    public static final BitSet FOLLOW_word_in_rule608 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule610 = new BitSet(new long[]{0x0000005000080012L});
    public static final BitSet FOLLOW_rule_attributes_in_rule621 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule631 = new BitSet(new long[]{0x0000000A00080012L});
    public static final BitSet FOLLOW_35_in_rule640 = new BitSet(new long[]{0x0000001000080012L});
    public static final BitSet FOLLOW_36_in_rule642 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule645 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_expander_lhs_block_in_rule663 = new BitSet(new long[]{0x0000000200080012L});
    public static final BitSet FOLLOW_normal_lhs_block_in_rule672 = new BitSet(new long[]{0x0000000200080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule695 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_37_in_rule699 = new BitSet(new long[]{0x0000001000080012L});
    public static final BitSet FOLLOW_36_in_rule701 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule705 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x0000000000000003L});
    public static final BitSet FOLLOW_33_in_rule751 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_import_statement_in_extra_statement773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_global_in_extra_statement778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_extra_statement783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_38_in_rule_attributes802 = new BitSet(new long[]{0x0000001000080012L});
    public static final BitSet FOLLOW_36_in_rule_attributes805 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule_attributes808 = new BitSet(new long[]{0x00001F8010000002L});
    public static final BitSet FOLLOW_28_in_rule_attributes815 = new BitSet(new long[]{0x00001F8000000000L});
    public static final BitSet FOLLOW_rule_attribute_in_rule_attributes820 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_rule_attributes822 = new BitSet(new long[]{0x00001F8010000002L});
    public static final BitSet FOLLOW_salience_in_rule_attribute861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_no_loop_in_rule_attribute871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_agenda_group_in_rule_attribute882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_duration_in_rule_attribute895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_activation_group_in_rule_attribute909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_auto_focus_in_rule_attribute920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_39_in_salience953 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_salience955 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_salience959 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_salience961 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_salience964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_no_loop999 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_no_loop1001 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_no_loop1003 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_no_loop1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_no_loop1031 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_BOOL_in_no_loop1035 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_no_loop1037 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_no_loop1039 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_no_loop1042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_auto_focus1088 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_auto_focus1090 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_auto_focus1092 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_auto_focus1095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_auto_focus1120 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_BOOL_in_auto_focus1124 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_auto_focus1126 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_auto_focus1128 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_auto_focus1131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_activation_group1173 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_activation_group1175 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_STRING_in_activation_group1179 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_activation_group1181 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_activation_group1184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_43_in_agenda_group1213 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_agenda_group1215 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_STRING_in_agenda_group1219 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_agenda_group1221 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_agenda_group1224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_duration1256 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_duration1258 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_duration1262 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_19_in_duration1264 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_duration1267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_normal_lhs_block1293 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_normal_lhs_block1295 = new BitSet(new long[]{0x7000000008080032L});
    public static final BitSet FOLLOW_opt_eol_in_normal_lhs_block1307 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_paren_chunk_in_expander_lhs_block1348 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EOL_in_expander_lhs_block1352 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_EOL_in_expander_lhs_block1372 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_lhs_or_in_lhs1415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fact_binding_in_lhs_column1443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fact_in_lhs_column1452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_fact_binding1484 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact_binding1494 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_fact_binding1496 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact_binding1498 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_fact_expression_in_fact_binding1502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_fact_expression1534 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact_expression1536 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_fact_expression_in_fact_expression1540 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact_expression1542 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_fact_expression1544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fact_in_fact_expression1555 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact_expression1557 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_set_in_fact_expression1570 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact_expression1575 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_fact_in_fact_expression1589 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_dotted_name_in_fact1628 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact1636 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_fact1644 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact1647 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_constraints_in_fact1653 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact1672 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_fact1676 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_fact1678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_opt_eol_in_constraints1710 = new BitSet(new long[]{0x0000000000080032L});
    public static final BitSet FOLLOW_constraint_in_constraints1715 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_predicate_in_constraints1718 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraints1726 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_constraints1728 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraints1730 = new BitSet(new long[]{0x0000000000080032L});
    public static final BitSet FOLLOW_constraint_in_constraints1733 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_predicate_in_constraints1736 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraints1744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_opt_eol_in_constraint1763 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_constraint1771 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraint1773 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_constraint1775 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraint1777 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_constraint1787 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraint1797 = new BitSet(new long[]{0x00FF800000080012L});
    public static final BitSet FOLLOW_set_in_constraint1805 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraint1892 = new BitSet(new long[]{0x01000000080003E0L});
    public static final BitSet FOLLOW_ID_in_constraint1910 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_enum_constraint_in_constraint1935 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_literal_constraint_in_constraint1967 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_retval_constraint_in_constraint1987 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_constraint2020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_literal_constraint2047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_literal_constraint2058 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOAT_in_literal_constraint2071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOL_in_literal_constraint2082 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_56_in_literal_constraint2094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_enum_constraint2125 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_enum_constraint2127 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_enum_constraint2131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_retval_constraint2160 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_paren_chunk_in_retval_constraint2165 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_retval_constraint2168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_predicate2186 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_predicate2188 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_predicate2192 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_57_in_predicate2194 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_predicate2196 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_paren_chunk_in_predicate2200 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_predicate2202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_paren_chunk2248 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_paren_chunk_in_paren_chunk2252 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_paren_chunk2254 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_27_in_paren_chunk22325 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_paren_chunk2_in_paren_chunk22329 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_paren_chunk22331 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_30_in_curly_chunk2400 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_curly_chunk_in_curly_chunk2404 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_31_in_curly_chunk2406 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_lhs_and_in_lhs_or2464 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_set_in_lhs_or2473 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_lhs_or2478 = new BitSet(new long[]{0x7000000008000020L});
    public static final BitSet FOLLOW_lhs_and_in_lhs_or2485 = new BitSet(new long[]{0x0000600000000002L});
    public static final BitSet FOLLOW_lhs_unary_in_lhs_and2525 = new BitSet(new long[]{0x0C00000000000002L});
    public static final BitSet FOLLOW_set_in_lhs_and2534 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_lhs_and2539 = new BitSet(new long[]{0x7000000008000020L});
    public static final BitSet FOLLOW_lhs_unary_in_lhs_and2546 = new BitSet(new long[]{0x0C00000000000002L});
    public static final BitSet FOLLOW_lhs_exist_in_lhs_unary2584 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_not_in_lhs_unary2592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_eval_in_lhs_unary2600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_column_in_lhs_unary2608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_lhs_unary2614 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_lhs_unary2616 = new BitSet(new long[]{0x7000000008000020L});
    public static final BitSet FOLLOW_lhs_in_lhs_unary2620 = new BitSet(new long[]{0x0000000000080012L});
    public static final BitSet FOLLOW_opt_eol_in_lhs_unary2622 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_lhs_unary2624 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_60_in_lhs_exist2654 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_27_in_lhs_exist2657 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_lhs_column_in_lhs_exist2661 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_lhs_exist2663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_column_in_lhs_exist2669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_61_in_lhs_not2699 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_27_in_lhs_not2702 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_lhs_column_in_lhs_not2706 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_lhs_not2709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_column_in_lhs_not2715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_62_in_lhs_eval2741 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_lhs_eval2745 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF2L,0x0000000000000003L});
    public static final BitSet FOLLOW_paren_chunk2_in_lhs_eval2753 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_lhs_eval2757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_dotted_name2789 = new BitSet(new long[]{0x8000000000400002L});
    public static final BitSet FOLLOW_22_in_dotted_name2795 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_dotted_name2799 = new BitSet(new long[]{0x8000000000400002L});
    public static final BitSet FOLLOW_63_in_dotted_name2808 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_64_in_dotted_name2810 = new BitSet(new long[]{0x8000000000000002L});
    public static final BitSet FOLLOW_ID_in_argument2840 = new BitSet(new long[]{0x8000000000000002L});
    public static final BitSet FOLLOW_63_in_argument2846 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_64_in_argument2848 = new BitSet(new long[]{0x8000000000000002L});
    public static final BitSet FOLLOW_ID_in_word2876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_word2888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_word2897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_word2909 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_word2920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_39_in_word2930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_40_in_word2938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_word2946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_word2957 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_word2968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_word2982 = new BitSet(new long[]{0x0000000000000002L});

}