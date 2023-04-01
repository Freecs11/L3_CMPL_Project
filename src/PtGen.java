
/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/

import java.io.*;

/**
 * classe de mise en oeuvre du compilateur
 * ======================================= (verifications semantiques +
 * production du code objet)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class PtGen {

	// constantes manipulees par le compilateur
	// ----------------------------------------

	private static final int

	// taille max de la table des symboles
	MAXSYMB = 300,

	// codes MAPILE :
	RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4, OU = 5, ET = 6, NON = 7, INF = 8, INFEG = 9,
	SUP = 10, SUPEG = 11, EG = 12, DIFF = 13, ADD = 14, SOUS = 15, MUL = 16, DIV = 17, BSIFAUX = 18,
	BINCOND = 19, LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23, ARRET = 24, EMPILERADG = 25,
	EMPILERADL = 26, CONTENUL = 27, AFFECTERL = 28, APPEL = 29, RETOUR = 30,

	// codes des valeurs vrai/faux
	VRAI = 1, FAUX = 0,

	// types permis :
	ENT = 1, BOOL = 2, NEUTRE = 3,

	// categories possibles des identificateurs :
	CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3, PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8,
	PRIVEE = 9,

	// valeurs possible du vecteur de translation
	TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// utilitaires de controle de type
	// -------------------------------
	/**
	 * verification du type entier de l'expression en cours de compilation (arret de
	 * la compilation sinon)
	 */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}

	/**
	 * verification du type booleen de l'expression en cours de compilation (arret
	 * de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

	// pile pour gerer les chaines de reprise et les branchements en avant
	// -------------------------------------------------------------------

	private static TPileRep pileRep;

	// production du code objet en memoire
	// -----------------------------------

	private static ProgObjet po;

	// COMPILATION SEPAREE
	// -------------------
	//
	/**
	 * modification du vecteur de translation associe au code produit +
	 * incrementation attribut nbTransExt du descripteur NB: effectue uniquement si
	 * c'est une reference externe ou si on compile un module
	 * 
	 * @param valeur : TRANSDON, TRANSCODE ou REFEXT
	 */
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// descripteur associe a un programme objet (compilation separee)
	private static Descripteur desc;

	// autres variables fournies
	// -------------------------

	// MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE
	// LETTRES
	public static String trinome = "BouhmadRachid_BoquainMathis_FallSerigne";

	private static int tCour; // type de l'expression compilee
	private static int vCour; // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)

	// TABLE DES SYMBOLES
	// ------------------
	//
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

	// it = indice de remplissage de tabSymb
	// bc = bloc courant (=1 si le bloc courant est le programme principal)
	private static int it, bc;

	// compteur pour les variables
	private static int cpt, identCour,identaff, addIdCour , cptlc,cptref;
	private static int idOuV; // 0 si id 1 si Val

	private static int cptpar;// pour les appels de proc
	/**
	 * utilitaire de recherche de l'ident courant (ayant pour code
	 * UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf : recherche de l'indice it vers borneInf (=1 si recherche
	 *                 dans tout tabSymb)
	 * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans
	 *         tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code : UtilLex.numIdCourant de l'ident
	 * @param cat  : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type : ENT, BOOL ou NEUTRE
	 * @param info : valeur pour une constante, ad d'exécution pour une variable,
	 *             etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 * utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() {
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}

	/**
	 * initialisations A COMPLETER SI BESOIN -------------------------------------
	 */
	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;

		cpt = 0; cptlc=0; cptpar=0;
		identCour = -1;
		identaff = -1 ;
		addIdCour = -1;
		idOuV = -1; 
		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep();
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();

		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();

		// initialisation du type de l'expression courante
		tCour = NEUTRE;

	} // initialisations

	/**
	 * code des points de generation A COMPLETER
	 * -----------------------------------------
	 * 
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {

		switch (numGen) {
		case 0:
			initialisations();
			break;

		case 1:
			tCour = ENT;
			vCour = UtilLex.valEnt;


			break;
		case 2:
			tCour = BOOL;
			vCour = VRAI;

			break;
		case 3:
			tCour = ENT;
			vCour = UtilLex.valEnt;


			break;

		case 4:
			tCour = ENT;
			vCour = -UtilLex.valEnt;

			break;

		case 5:
			identCour = presentIdent(1);

			if(idOuV==0 && idOuV != -1) {
				po.produire(EMPILER);
				po.produire(vCour);
			}
			else if (identCour != -1) {
				int categor = tabSymb[identCour].categorie;
				switch (categor) {
				case CONSTANTE:
					po.produire(EMPILER);
					po.produire(tabSymb[identCour].info);
					break;
				case VARGLOBALE:
					po.produire(CONTENUG);
					po.produire(tabSymb[identCour].info);
					break;

				case VARLOCALE,PARAMFIXE:
					po.produire(CONTENUL);
				po.produire(tabSymb[identCour].info);
				po.produire(0);
				break;
				case PARAMMOD:
					po.produire(CONTENUL);
					po.produire(tabSymb[identCour].info);
					po.produire(1);
					break;
				default:

					break;


				}
			}
			identCour = -1;

			break;

		case 50 :
			// pour les verifications des entiers
			verifEnt();


			break;

		case 51:
			// pour les verifications des bool
			verifBool();


			break;
		case 40:
			identCour = presentIdent(1);
			idOuV=1;
			int typIdent = tabSymb[identCour].type;
			if(typIdent==ENT) {
				tCour=ENT;
			}
			if (typIdent==BOOL) {
				tCour=BOOL;
			}

			break;
		case 43:
			break;
		case 41:
			idOuV = 0;

			break;
		case 42:
			idOuV = -1;
			identCour = -1;
			break;
		case 6:
			identCour = presentIdent(1);
			if (identCour == -1) {
				// verifEnt();
				if (tCour == ENT && idOuV != -1) {
					po.produire(EMPILER);
					po.produire(vCour);

				}
			} else {
				int categor = tabSymb[identCour].categorie;
				switch (categor) {
				case CONSTANTE:
					po.produire(EMPILER);
					po.produire(tabSymb[identCour].info);
					break;
				case VARGLOBALE:
					po.produire(CONTENUG);
					po.produire(tabSymb[identCour].info);
					break;
				case VARLOCALE,PARAMFIXE:
					po.produire(CONTENUL);
				po.produire(tabSymb[identCour].info);
				po.produire(0);
				break;
				case PARAMMOD:
					po.produire(CONTENUL);
					po.produire(tabSymb[identCour].info);
					po.produire(1);
					break;
				default:

					break;

				}
			}
			po.produire(MUL);
			tCour=ENT;
			// identCour=-1;

			break;

		case 7:
			identCour = presentIdent(1);
			if (identCour == -1) {

				if (tCour == ENT && idOuV != -1 && idOuV!=1) {
					po.produire(EMPILER);
					po.produire(vCour);
				}
			} else {
				int categor = tabSymb[identCour].categorie;
				switch (categor) {
				case CONSTANTE:
					po.produire(EMPILER);
					po.produire(tabSymb[identCour].info);
					break;
				case VARGLOBALE:
					po.produire(CONTENUG);
					po.produire(tabSymb[identCour].info);
					break;
				case VARLOCALE,PARAMFIXE:
					po.produire(CONTENUL);
				po.produire(tabSymb[identCour].info);
				po.produire(0);
				break;
				case PARAMMOD:
					po.produire(CONTENUL);
					po.produire(tabSymb[identCour].info);
					po.produire(1);
					break;
				default:

					break;
				}
			}

			po.produire(DIV);
			identCour=-1;
			idOuV=-1;
			tCour=ENT;
			break;

		case 8:

			po.produire(ADD);
			identCour=-1;
			tCour=ENT;

			break;
		case 9:
			// verifEnt();

			po.produire(SOUS);
			identCour=-1;
			tCour=ENT;

			break;

		case 10:
			int id = presentIdent(1);
			if(bc>1) {
				if(presentIdent(bc)==0) {
					placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);	
				}else {
					UtilLex.messErr("constante existante , Local");
				}
			}
			else {
				if (id == 0) {
					placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);
				}
			}
			break;

		case 11:
			if(bc>1) {
				
				if(presentIdent(bc)==0) {
					placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, tabSymb[bc-1].info+2);cptlc++;	
				}else {
					UtilLex.messErr("variable existante");
				}
			}else {
				int idV = presentIdent(1);

				if (idV == 0) {
					placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, cpt);
					cpt++;

				}else {
					UtilLex.messErr("variable existante");
				}
			}
			break;
		case 101:
			cptlc=0;
			break;
		case 100:
			if(bc>1) {
				if(presentIdent(bc)==0) {
					placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, tabSymb[it].info+1);cptlc++;	
				}
			}else {
				int idV = presentIdent(1);

				if (idV == 0) {
					placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, cpt);
					cpt++;

				}
			}
			break;
		case 12:
			tCour = ENT;
			break;
		case 13:
			tCour = BOOL;
			break;

		case 14: // faux
			tCour = BOOL;
			vCour = FAUX;
			break;

		case 15:

			po.produire(EG);
			tCour=BOOL;

			break;
		case 16:

			po.produire(DIFF);
			tCour=BOOL;

			break;
		case 17:

			po.produire(SUP);
			tCour=BOOL;

			break;
		case 18:

			po.produire(SUPEG);
			tCour=BOOL;

			break;
		case 19:

			po.produire(INF);
			tCour=BOOL;

			break;
		case 20:

			po.produire(INFEG);
			tCour=BOOL;

			break;
		case 21:

			po.produire(NON);
			tCour=BOOL;

			break;
		case 22:

			po.produire(ET);
			tCour=BOOL;

			break;
		case 23:

			po.produire(OU);
			tCour=BOOL;

			break;

		case 24:
			int idId = presentIdent(1);
			if (idId != 0 && idOuV!=-1 && idOuV!=1) {
				int category = tabSymb[idId].categorie;
				switch (category) {
				case CONSTANTE:
					po.produire(EMPILER);
					po.produire(tabSymb[idId].info);
					break;
				case VARGLOBALE:
					po.produire(CONTENUG);
					po.produire(tabSymb[idId].info);
					break;
				case VARLOCALE,PARAMFIXE:
					po.produire(CONTENUL);
					po.produire(tabSymb[idId].info);
					po.produire(0);
				break;
				case PARAMMOD:
					po.produire(CONTENUL);
					po.produire(tabSymb[idId].info);
					po.produire(1);
					break;

					// les autres catégories restent à traité .

				default:
					break;
				}
			}
			identCour=-idId;
			idOuV=1;
			break;

		case 25: // pour réservé les variables
			if(bc>1) {
				po.produire(RESERVER);
				po.produire(cptlc);
			}else {
				po.produire(RESERVER);
				po.produire(cpt);
				desc.setTailleGlobaux(cpt);
			}
			break;

		case 26:
			if(identaff!=-1) {
				if(bc>1) {
					// en proc
					switch(tabSymb[identaff].categorie) {
					case PARAMMOD:
						po.produire(AFFECTERL);
						po.produire(addIdCour);
						po.produire(1);
						addIdCour = -1;
						break;
					case VARLOCALE:
						po.produire(AFFECTERL);
						po.produire(addIdCour);
						po.produire(0);
						addIdCour = -1;
						break;
					case VARGLOBALE:
						po.produire(AFFECTERG);
						po.produire(addIdCour);
						addIdCour = -1;
					}

				}else {
					//en globale
					if (addIdCour != -1) {
						po.produire(AFFECTERG);
						po.produire(addIdCour);
						addIdCour = -1;
					} else {
						UtilLex.messErr("erreur dans l'@ de ident ");
					}
				}
			}
			identaff=-1;

			break;
		case 27:
			int id_t = presentIdent(1);
			identCour = id_t;
			identaff= id_t;

			if (id_t != 0) {
				int category = tabSymb[id_t].categorie;
				switch (category) {
				case CONSTANTE:
					UtilLex.messErr("afffectation à une constante");
					break;
				case PARAMFIXE:
					UtilLex.messErr("afffectation à un Param fixe");
					break;
				case PRIVEE,PROC:
					UtilLex.messErr("syntaxe incorrecte");
				break;

				default:
					addIdCour = tabSymb[id_t].info;
					break;
				}
			}
			idOuV=1;
			break;

		case 28:
			if (idOuV == -1) {
				if (tCour == ENT) {
					po.produire(ECRENT);
				} else if (tCour == BOOL) {
					po.produire(ECRBOOL);
				} else {
					UtilLex.messErr("pas d'instruction pour le type neutre");
				}
			} else if (idOuV == 1) {
				identCour = presentIdent(1);
				int type = tabSymb[identCour].type;
				if (type == ENT) {
					po.produire(ECRENT);
				} else if (type == BOOL) {
					po.produire(ECRBOOL);
				} else {
					UtilLex.messErr("pas d'instruction pour le type neutre");
				}
			} else {
				//UtilLex.messErr("Erreur dans ecrire");
			}

			break;
		case 29:
			// lire
			identCour = presentIdent(1);
			int category = tabSymb[identCour].categorie;
			int type = tabSymb[identCour].type;
			switch (category) {
			case CONSTANTE:
				UtilLex.messErr("affectation à une constante");
				break;
			case VARGLOBALE:
				if (type == ENT) {
					po.produire(LIRENT);
					po.produire(AFFECTERG);
					po.produire(tabSymb[identCour].info);

				} else if (type == BOOL) {
					po.produire(LIREBOOL);
					po.produire(AFFECTERG);
					po.produire(tabSymb[identCour].info);

				} else {
					UtilLex.messErr("pas d'instruction pour le type neutre");
				}
				break;
			case VARLOCALE :
				if (type == ENT) {
					po.produire(LIRENT);
					po.produire(AFFECTERL);
					po.produire(tabSymb[identCour].info);
					po.produire(0);
				} else if (type == BOOL) {
					po.produire(LIREBOOL);
					po.produire(AFFECTERL);
					po.produire(tabSymb[identCour].info);
					po.produire(0);

				} else {
					UtilLex.messErr("pas d'instruction pour le type neutre");
				}
				break;
			case PARAMMOD:
				if (type == ENT) {
					po.produire(LIRENT);
					po.produire(AFFECTERL);
					po.produire(tabSymb[identCour].info);
					po.produire(1);
				} else if (type == BOOL) {
					po.produire(LIREBOOL);
					po.produire(AFFECTERL);
					po.produire(tabSymb[identCour].info);
					po.produire(1);

				} else {
					UtilLex.messErr("pas d'instruction pour le type neutre");
				}
				break;
			default:
				UtilLex.messErr("err ecrire");
			}


			break;

		case 30:
			// SI debut
			po.produire(BSIFAUX);
			po.produire(0);
			pileRep.empiler(po.getIpo());
			break;
		case 31:
			System.out.println("executé");
			// sinon
			po.produire(BINCOND);
			po.produire(0);
			int si = pileRep.depiler();
			po.modifier(si, po.getIpo() + 1);
			pileRep.empiler(po.getIpo());
			break;
		case 32:
			// fsi
			int fsi = pileRep.depiler();
			po.modifier(fsi, po.getIpo() + 1);

			break;

		case 33:
			// ttq debut
			pileRep.empiler(po.getIpo() + 1);
			break;
		case 34:
			// ttq faire
			po.produire(BSIFAUX);
			po.produire(0);
			pileRep.empiler(po.getIpo());

			break;
		case 35:
			// ttq fin
			int ttq = pileRep.depiler();
			po.modifier(ttq, po.getIpo() + 3);
			po.produire(BINCOND);
			int ttq_base = pileRep.depiler();
			po.produire(ttq_base);
			break;
		case 60:
			pileRep.empiler(0);
			break;
		case 61:
			verifBool();
			po.produire(BSIFAUX);
			po.produire(0);
			pileRep.empiler(po.getIpo());
			break;
		case 62:
			po.modifier(pileRep.depiler(), po.getIpo()+3 );
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			pileRep.empiler(po.getIpo());
			break;

		case 63 :
			po.modifier(pileRep.depiler(), po.getIpo()+1 );
			break;

		case 64:
			int t = pileRep.depiler();
			int l = po.getElt(t);
			po.modifier(t, po.getIpo()+1 );
			t=l;
			l = po.getElt(t);

			while(t!=0) {
				po.modifier(t, po.getIpo()+1 );
				t=l;
				l = po.getElt(t);
			}
			break;

		case 70:
			placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, po.getIpo()+1);
			placeIdent(-1, PRIVEE, NEUTRE, 0);
			bc=it+1;
			break;

		case 71 :
			po.produire(BINCOND);
			po.produire(0);
			pileRep.empiler(po.getIpo());
			break;

		case 73 : 

			if(presentIdent(bc)==0) {
				placeIdent(UtilLex.numIdCourant, PARAMFIXE, tCour,cptlc);
				cptlc++;
			}else {
				UtilLex.messErr("Param déjà déclaré");
			}

			break;
		case 74:
			if(presentIdent(bc)==0) {
				placeIdent(UtilLex.numIdCourant, PARAMMOD, tCour,cptlc);
				cptlc++;
			}else {
				UtilLex.messErr("Param déjà déclaré");
			}

			break;


		case 75:
			tabSymb[bc-1].info=cptlc;
			if(desc.getUnite()=="module") {
			int adProc = desc.presentDef(UtilLex.chaineIdent(tabSymb[bc-2].code));
			if(adProc!= 0) {
				desc.modifDefAdPo(adProc,tabSymb[bc-2].info);
				desc.modifDefNbParam(adProc, cptlc);
			}else {
				UtilLex.messErr("Proc non déclarer");
			}
			}
			break;
		case 76 :
			po.modifier(pileRep.depiler(), po.getIpo()+1);
			break;
		case 77:
			int nb = tabSymb[bc-1].info;
			if(nb==0 && cptlc==0) {
				it=bc-1;
				bc = 0;
			}else {
			System.out.println("bc = "+bc + " and nb = "+nb );
			
			it=bc-1+nb;
			
				for (int i =0; i < nb; i++) {
				tabSymb[bc+i].code=-1;
				}
			
			}
			bc =0;
			cptlc=0;
			po.produire(RETOUR);
			po.produire(nb);
			break;
			
		case 78:
			cptpar++;
			break;
			
			
		case 80:
			cptpar++;
			int idfd = presentIdent(1);
			if(idfd==0) {
				UtilLex.messErr("variable n'éxiste pas ");
			}else {
				switch (tabSymb[idfd].categorie) {
				case CONSTANTE,PARAMFIXE:
					UtilLex.messErr("PASSAGE de mauvais type en param mod ");
					break;
				case VARGLOBALE:	
					po.produire(EMPILERADG);
					po.produire(tabSymb[idfd].info);
					break;
				case VARLOCALE:
					po.produire(EMPILERADL);
					po.produire(tabSymb[idfd].info);
					po.produire(0);
					break;
				case PARAMMOD:
					po.produire(EMPILERADL);
					po.produire(tabSymb[idfd].info);
					po.produire(1);
					break;
				default:
					UtilLex.messErr("PASSAGE de mauvais type en param mod ");
					break;
				}
			}
			break;
		case 81:
			if(cptpar!=tabSymb[identaff+1].info) {
				UtilLex.messErr("Nombre de paramètre différent " + cptpar + " - " + tabSymb[identaff].info );
			}
			po.produire(APPEL);
			po.produire(tabSymb[identaff].info);
			po.produire(cptpar);
			
			cptpar=0;
			break;
		case 82:
			identaff=presentIdent(1);
			if(identaff==0) {
				UtilLex.messErr("Procédure n'éxiste pas ");
			}
			break;
		case 110:
			desc.setUnite("programme");
			break;
		case 111:
			desc.setUnite("module");
			break;
			
		case 115 : 
			desc.ajoutDef(UtilLex.chaineIdent(UtilLex.numIdCourant));
			break;
		case 116 : 
			desc.ajoutRef(UtilLex.chaineIdent(UtilLex.numIdCourant));
			cptref=1;
			break;
		case 117 : 
			cptref++;
			break;
		case 118 : 
			int adRef = desc.presentRef(UtilLex.chaineIdent(UtilLex.numIdCourant));
			desc.modifRefNbParam(adRef, cptref);
			break;
		case 254:
			po.produire(ARRET);
			desc.setTailleCode(po.getIpo());
			desc.ecrireDesc(System.class.getName());
			po.constObj();
			po.constGen();
			break;
		case 255:
			afftabSymb(); // affichage de la table des symboles en fin de compilation
			break;

		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;

		}
	}
}
