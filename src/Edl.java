import java.io.*;
import java.util.Iterator;


 //TODO : Renseigner le champs auteur : Nom1_Prenom1_Nom2_Prenom2_Nom3_Prenom3
 /**
 * 
 * @author XXX, YYY, ZZZ
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
	private static final int TRANSDON=1,TRANSCODE=2,REFEXT=3;

	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc = new Descripteur[MAXMOD + 1];
	
	
	//TODO : declarations de variables A COMPLETER SI BESOIN
	static int ipo, nMod, nbErr,varGlobTotal;
	static String nomProg;
	static Descripteur.EltDef[] dicoDef = new Descripteur.EltDef[60];
	static String[] files= new String[MAXMOD+1]; 
	static int[] transDon ;
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
		varGlobTotal=0;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		files[0]=s;
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");
		nomProg = s;
		int rem=1;
		varGlobTotal+=tabDesc[0].getTailleGlobaux();
		
		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1)
					+ " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				nMod = nMod + 1;
				files[rem]=s;
				rem++;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);

				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
				varGlobTotal+=tabDesc[nMod].getTailleGlobaux();
			}
		}
		transCode = new int[nMod+1];
		transDon = new int[nMod+1];
		
	}


	static void constMap() {
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomProg
					+ ".map impossible");
		// pour construire le code concatene de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];
		
		ipo = 0;
		
		// indice 0 -> reserver nb
		// parcours fichier par fichier
		for (int i = 0; i < nMod+1; i++) {
			InputStream file = Lecture.ouvrir(files[i] + ".obj");
			int [] vTrans = new int[1000];
			int vRem= 0;
			// nbTransExt
			int nbTransExt = tabDesc[i].getNbTransExt();
			for (int j = 0; j < nbTransExt; j++) {
				vTrans[vRem]= Lecture.lireInt();
				vRem++;
				vTrans[vRem]= Lecture.lireInt();
				vRem++;
			}
			
			
			for (int j = nbTransExt+1; j < tabDesc[i].getTailleCode()-1; j++) {
				po[ipo] = Lecture.lireIntln(file);
				for (int k = 0; k < vTrans.length; k+=2) {
					if(po[ipo]==vTrans[k]) {
						switch (vTrans[k+1]) {
						case TRANSCODE:
							po[ipo]=transCode[i];
							break;
						case TRANSDON:
							po[ipo]=transDon[i];
							break;
						case REFEXT:
							po[ipo]=adFinale[i][j];
							break;
				
						default:
							break;
						}
					}
				}
				ipo++;
			}
			
		}
		po[1]=varGlobTotal;

		Ecriture.fermer(f2);

		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(ipo, po, nomProg + ".ima");
	}
	
	static void constTabsCodeDon() {
		
		int x=1 ;
		int k=0;
		transCode[0]=0;
		transDon[0]=0;
		for (int i = 1; i < nMod+1; i++) {
			// transcode et transdon
			if(i>0) {
			transCode[x]=transCode[x-1]+tabDesc[i-1].getTailleCode();
			transDon[x]=tabDesc[i-1].getTailleGlobaux();
			}
			x++;
		}
		
	}
	static void constAdFinale() {
		for (int i = 0; i < nMod+1; i++) {
			// accede à tabRef 
			System.out.print("indice "+i + " ");
			for (int j = 0; j < tabDesc[i].getNbRef()+1; j++) {
				// j -> tabref
				boolean found =false ;
				
				for (int k =0; k < dicoDef.length; k++) {
					if(dicoDef[k]!=null) {
//						System.out.println(dicoDef[k].nomProc + "  ----  " + tabDesc[i].getRefNomProc(j));
//						System.out.println(dicoDef[k].adPo +  "  ----  " + tabDesc[i].getRefNbParam(j));
						if(dicoDef[k].nomProc.compareTo(tabDesc[i].getRefNomProc(j))==0
						&&	dicoDef[k].nbParam==tabDesc[i].getRefNbParam(j))
						{
							adFinale[i][j]=dicoDef[k].adPo;
							System.out.print(adFinale[i][j] + " ");
							found = true;
						}
					}
				}
				if(!found) {
					erreur(NONFATALE, " ref non trouvé dans dicoDef" );
				}
				found = false;
			}	
			System.out.println();
		}
	}
	
	static void constDicoDef() {
		int x=0; // x -> indice de remplissage de dicoDef
		for (int i = 0; i < nMod+1; i++) {
			// i -> module
			
			for (int j = 1; j < tabDesc[i].getNbDef()+1; j++) {
				dicoDef[x] = tabDesc[i].tabDef[j];
				dicoDef[x].adPo+= transCode[i]; 
				System.out.println(dicoDef[x].nomProc + " , "+ dicoDef[x].adPo +" , "+ dicoDef[x].nbParam);
				x++;
			}
			
		}
		
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;

		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs();		//TODO : lecture des descripteurs a completer si besoin

		constTabsCodeDon();
		constDicoDef();
		constAdFinale();
		
		for (int i = 0; i < nMod+1; i++) {
			System.out.println(files[i]);
		}
		
		
		
		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}

		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap();				//TODO : ... A COMPLETER ...
		System.out.println("Edition de liens terminee");
	}
}