import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//TODO : Renseigner le champs auteur : Nom1_Prenom1_Nom2_Prenom2_Nom3_Prenom3
/**
 * 
 * @author Bouhmad_Rachid,Boquain_Mathis,Fall_Serigne
 * @version 2020
 *
 */

public class Edl {

	// nombre max de modules, taille max d'un code objet d'une unite
	static final int MAXMOD = 5, MAXOBJ = 1000;
	// nombres max de references externes (REF) et de points d'entree (DEF)
	// pour une unite
	private static final int MAXREF = 10, MAXDEF = 10;

	// typologie des erreurs
	private static final int FATALE = 0, NONFATALE = 1;

	// valeurs possibles du vecteur de translation
	private static final int TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc = new Descripteur[MAXMOD + 1];

	// TODO : declarations de variables A COMPLETER SI BESOIN
	static int ipo, nMod, nbErr, varGlobTotal;
	static String nomProg;
	static Descripteur.EltDef[] dicoDef = new Descripteur.EltDef[60];
	static String[] files = new String[MAXMOD + 1];
	static int[] transDon;
	static int[] transCode;
	static int[][] adFinale = new int[MAXMOD][MAXDEF];

	// utilitaire de traitement des erreurs
	// ------------------------------------
	static void erreur(int te, String m) {
		System.out.println(m);
		if (te == FATALE) {
			System.out.println("ABANDON DE L'EDITION DE LIENS");
			System.exit(1);
		}
		nbErr = nbErr + 1;
	}

	// utilitaire de remplissage de la table des descripteurs tabDesc
	// --------------------------------------------------------------
	static void lireDescripteurs() {
		String s;
		varGlobTotal = 0;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		nomProg = s;
		files[0] = s;
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");

		int rem = 1;
		varGlobTotal += tabDesc[0].getTailleGlobaux();

		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1) + " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				nMod = nMod + 1;
				files[rem] = s;
				rem++;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);

				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
				varGlobTotal += tabDesc[nMod].getTailleGlobaux();
			}
		}
		transCode = new int[nMod + 1];
		transDon = new int[nMod + 1];

	}

	/**
	 * Rempli les tableaux transDon et transCode
	 */
	static void constTabsCodeDon() {
		int x = 1;
		transCode[0] = 0;
		transDon[0] = 0;
		for (int i = 1; i < nMod +1; i++) {
			// transcode et transdon
			if (i > 0) {
				transCode[x] = transCode[x - 1] + tabDesc[i - 1].getTailleCode();
				transDon[x] = transDon[x-1]+ tabDesc[i-1].getTailleGlobaux();
			}
			x++;
		}

	}
	
	/**
	 * Rempli le tableau dicoDef
	 */
	static void constDicoDef() {
		int x = 0; // x -> indice de remplissage de dicoDef
		for (int i = 0; i < nMod + 1; i++) {
			// i -> indice du fichier 
			for (int j = 1; j < tabDesc[i].getNbDef() + 1; j++) {
				dicoDef[x] = tabDesc[i].tabDef[j];
				dicoDef[x].adPo += transCode[i];
				x++;
			}

		}

	}
	
	/**
	 *  Rempli le tableau adFinale
	 */
	static void constAdFinale() {
		for (int i = 0; i < nMod + 1; i++) { // indice du fichier
			System.out.print("indice " + i + " : ");
			for (int j = 0; j < tabDesc[i].getNbRef() + 1; j++) { // accede à tabRef du fichier d'indice i
				boolean found = false;
				for (int k = 0; k < dicoDef.length; k++) { 
					if (dicoDef[k] != null) {
						if (dicoDef[k].nomProc.compareTo(tabDesc[i].getRefNomProc(j)) == 0
								&& dicoDef[k].nbParam == tabDesc[i].getRefNbParam(j)) {
							adFinale[i][j] = dicoDef[k].adPo;
							System.out.print(adFinale[i][j] + " ");
						}
					}
				}
			}
			System.out.println();
		}
	}
	
	/**
	 * vérifie si les procédures rencontrés exist  
	 */
	static void procsExists() {
	    Set<String> allProcNames = new HashSet<>(); 
	    for (int i = 0; i < (nMod + 1); i++) {
	        for (int j = 1; j <= tabDesc[i].getNbRef(); j++) {
	            String nomProc = tabDesc[i].getRefNomProc(j);
	            allProcNames.add(nomProc);  // Set donc les doublons ne poseront pas problème
	        }
	    }
	    for (int i = 0; i < (nMod + 1); i++) {
	        for (int j = 0; j <= tabDesc[i].getNbDef(); j++) {
	        	
	            String nomProc = tabDesc[i].getDefNomProc(j);
	            if (nomProc!="inconnu" && !allProcNames.contains(nomProc) ) {
	                erreur(NONFATALE, "la procédure " + nomProc + " n'est pas presente ");
	            }
	        	
	        }
	    }
	}


	static void constMap() {
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomProg + ".map impossible");
		// pour construire le code concatene de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];

		ipo = 0; 
		int lu;
		InputStream f;
		int taille = 0;
		for (int index = 0; index < nMod + 1; index++) {  // index est l'indice du fichier
			
			int[] vTrans = new int[tabDesc[index].getNbTransExt() * 2]; 

			if (index == 0) {
				f = Lecture.ouvrir(nomProg + ".obj");
			} else {
				f = Lecture.ouvrir(files[index] + ".obj");
				ipo--;
			}
			for (int i = 0; i < vTrans.length; i += 2)  // on rempli le tableau vTrans on lisant nbTransExt au début du fichier
			{
				vTrans[i] = ipo+ Lecture.lireInt(f);
				vTrans[i + 1] = Lecture.lireIntln(f);
			}

			if (index == 0) {
				int reserver = Lecture.lireInt(f);
				ipo++;
				po[ipo] = reserver;
				ipo++;
				po[ipo] = varGlobTotal;
				Ecriture.ecrireStringln(f2, "" + reserver);
				Ecriture.ecrireStringln(f2, "" + varGlobTotal);
				Lecture.lireInt(f);  // ignore le nb variable globale dans le fichier puisqu'on l'a changé
			}
			taille += tabDesc[index].getTailleCode();
			ipo++;
			while (ipo <= taille) {
				lu = Lecture.lireInt(f);
				po[ipo] = lu;
				for (int j = 0; j < vTrans.length; j += 2) {
					if (ipo == vTrans[j]) {
						switch (vTrans[j + 1]) {
						case TRANSCODE:
							po[ipo] += transCode[index];
							break;
						case TRANSDON:
							po[ipo] +=  transDon[index];
							break;
						case REFEXT:
							po[ipo] = adFinale[index][lu];
							break;
						default:

							break;
						}
					}
				}

				Ecriture.ecrireStringln(f2, "" + lu);
				ipo++;
			}
		}
		ipo--;
		Ecriture.fermer(f2);

		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(ipo, po, nomProg + ".ima");
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;

		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs(); // TODO : lecture des descripteurs a completer si besoin

		constTabsCodeDon();
		constDicoDef();
		constAdFinale();
		procsExists();
		
//		for (int i = 0; i < nMod + 1; i++) {
//			System.out.println(files[i]);
//		}

		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}

		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap(); // TODO : ... A COMPLETER ...
		System.out.println("Edition de liens terminee");
	}
}