// Grammaire du langage PROJET
// CMPL L3info 
// Nathalie Girard, Veronique Masson, Laurent Perraudeau
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog  EOF {PtGen.pt(254);}
      |    unitmodule  EOF {PtGen.pt(254);} 
  ;
  
unitprog
  : 'programme' ident ':'  {PtGen.pt(110);}
     declarations  
     corps { System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' ident ':' {PtGen.pt(111);}
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident  {PtGen.pt(115);} (',' ident {PtGen.pt(115);} )* ptvg 
  ;
  
partieref: 'ref'  specif  (',' specif  )* ptvg
  ;
  
specif  : ident {PtGen.pt(116);} ( 'fixe'  '(' type {PtGen.pt(117);} ( ',' type {PtGen.pt(117);} )*  ')'  )?
                 ( 'mod'  '(' type {PtGen.pt(117);} ( ',' type {PtGen.pt(117);} )*  ')' )? {PtGen.pt(118);}
  ;
  
consts  : 'const' ( ident  '=' valeur  {PtGen.pt(10);} ptvg   )+ 
  ;
  
// VARLOCAL etc à compléter
vars  : 'var' ( type ident  {PtGen.pt(11);} ( ','  ident {PtGen.pt(100);} )* ptvg  )+ {PtGen.pt(25);}
  ;
  
type  : 'ent'  {PtGen.pt(12);}
  |     'bool' {PtGen.pt(13);}
  ;
  
decprocs:{ PtGen.pt(71); } (decproc ptvg)+ {PtGen.pt(76);}
  ;
  
decproc :  'proc'   ident { PtGen.pt(70); }  parfixe? parmod? { PtGen.pt(75); } consts? {PtGen.pt(101);} vars?  corps {PtGen.pt(77);} { PtGen.pt(255); }
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin' 
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident  { PtGen.pt(73); } ( ',' ident  { PtGen.pt(73); } )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident { PtGen.pt(74); } ( ',' ident  { PtGen.pt(74); } )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  |
  ;
  
inssi : 'si' expression { PtGen.pt(30); } 'alors' instructions  ( 'sinon' {PtGen.pt(31); } instructions )?  'fsi'{ PtGen.pt(32); }
  ;
  
inscond : 'cond' { PtGen.pt(60); }  expression { PtGen.pt(61); }  ':' instructions 
          (',' { PtGen.pt(62); } expression { PtGen.pt(61); } ':' instructions )  * 
          ('aut' { PtGen.pt(62); } instructions  | { PtGen.pt(63); }  ) 
          'fcond' { PtGen.pt(64); }
  ;
  
boucle  : 'ttq'  { PtGen.pt(33); } expression 'faire'{ PtGen.pt(34); } instructions 'fait'  { PtGen.pt(35 ); }
  ;
  
lecture: 'lire' '(' ident  { PtGen.pt(29); } ( ',' ident  { PtGen.pt(29); } )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression { PtGen.pt(28); } ( ',' expression   { PtGen.pt(28); } )* ')'
   ;
  
affouappel
  : ident (  { PtGen.pt(27); }   ':=' expression { PtGen.pt(26); }
            | { PtGen.pt(82); }  (effixes (effmods)?)?  { PtGen.pt(81); } 
           )
  ;
  
effixes : '(' ( expression  { PtGen.pt(78); }  (',' expression  { PtGen.pt(78); }  )*)? ')'
  ;
  
effmods :'(' (ident { PtGen.pt(80); }  (',' ident { PtGen.pt(80); }  )*)? ')'
  ; 
  
expression: (exp1) ('ou' { PtGen.pt(51); } exp1 { PtGen.pt(23); } )* 
  ;
  
exp1  : exp2 ('et' { PtGen.pt(51); }  exp2 { PtGen.pt(51); }  { PtGen.pt(22); } )*
  ;
  
exp2  : 'non' exp2 { PtGen.pt(51); }  { PtGen.pt(21); }
  | exp3  
  ;
  
exp3  : exp4 
  ( '='  { PtGen.pt(50); }   exp4  { PtGen.pt(50); }  { PtGen.pt(15); }
  | '<>' { PtGen.pt(50); }   exp4  { PtGen.pt(50); }  { PtGen.pt(16); }
  | '>'   { PtGen.pt(50); }  exp4  { PtGen.pt(50); }  { PtGen.pt(17); }
  | '>='  { PtGen.pt(50); }  exp4  { PtGen.pt(50); }  { PtGen.pt(18); }
  | '<'   { PtGen.pt(50); }  exp4  { PtGen.pt(50); }  { PtGen.pt(19); }
  | '<='  { PtGen.pt(50); }  exp4  { PtGen.pt(50); }   { PtGen.pt(20); }
  ) ?
  ;
  
exp4  : exp5 
        ('+'  { PtGen.pt(50); }   exp5 { PtGen.pt(50); } { PtGen.pt(8); }
        | '-' { PtGen.pt(50); }  exp5 { PtGen.pt(50); } { PtGen.pt(9); }
        )*
  ;
  
exp5  : primaire { PtGen.pt(5); }
        (   '*'  { PtGen.pt(50); }  primaire { PtGen.pt(50); } { PtGen.pt(6); }
          | 'div' { PtGen.pt(50); }  primaire { PtGen.pt(50); } { PtGen.pt(7); }
        )*
  ;
  
primaire: valeur  {PtGen.pt(41);}
  | ident  {PtGen.pt(40);}
  |'('{PtGen.pt(43);} expression ')'{PtGen.pt(42);}
  ;
  
valeur  : nbentier { PtGen.pt(1); }
  | '+' nbentier { PtGen.pt(3); }
  | '-' nbentier { PtGen.pt(4); }
  | 'vrai' { PtGen.pt(2); }
  | 'faux' { PtGen.pt(14); }
  ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	   



	   
