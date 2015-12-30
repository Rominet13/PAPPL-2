/*
 * Résumé: Classe lisant le ficher .an et creéant le fichier .json correspondant
 * avec automatiquement le même nom (seul l'extension change.)
 * Le nom du fichier .an à lire est rentré en paramétre, sans son extension .an. 
 * 
 * Code: 4 méthodes "static"  (car l'utilisation est ponctuelle et ne nécessite pas d'attribut. On raisonne peut-être plus en impératif qu'en objet.)
 *     *lectureAn   -la  1ère lit le fichier .an, avec en paramétre son nom privé de son extension. Et elle stoque tout le texte dans un String ("contenu") qu'elle retourne.  
 *     *ecritureStringJson  -la 2nd "parse" (ou traite) le texte de "contenu" en paramétre et écrit le futur contenu du fichier .json dans des Strings intermédiaires (variables locales: gene, coop et fleche).
 *          Elle fonctionne donc en 2 étapes: remplit d'abord gene, puis coop et fleche en même temps.
 *          Elle retourne un String (voir si pas plutot un tableau de String mieux? version futur) correpondant à la "concaténation"(moyennant 2-3 lignes en plus) dans l'odre de gene, coopt et fleche.  
 *     *ecritureJson   -la 3ieme crée et écrit le .json avec le String retourné par la méthode 2.
 *     *methode (ouais je sais: c'est très original^^) -la 4ieme appelle les autres méthodes dans l'ordre de manière à exécuter l'opération de conversion en 1 appel.
 *
 *  // peut être plus pratique avec l'utilisation des classes StringBuffer et StringBuilder (plus flexible, on peut modifier la chaine)
 *
 */
package conversionanjson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Romain
 */
public class AnToJson {

    private static Logger logger = Logger.getLogger("Logger");

    public static void methode(String nomAN) {
//        ecritureJson(ecritureStringJson(lectureAn(nomAN), nomAN), nomAN);
        System.out.println(ecritureStringJson(lectureAn(nomAN), nomAN));
    }

    /**
     * Méthode static destiné à être appeler par la méthode principale "methode"
     * de la classe AnToJson
     *
     * @param nomAN type String, correspondant au nom sans son extension ".an"
     * du fichier .an à lire
     * @return contenu type String, correspondant exactement au texte du fichier
     * .an
     */
    private static String lectureAn(String nomAN) {
        String contenu = null; // null parceque sinon netbeans détecte qu'il n'a potentiellement pas été initialisé....
        String tmp; // pour stocker la ligne courante en même temps qu'on fait le test
        //tmp est initialisé et changer dans la condition du while. Il y a surement mieux à faire.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(nomAN + ".an")); //ouverture du fichier à lire
            logger.info("Ficher " + nomAN + ".an ouvert.");

            contenu = reader.readLine();   // initialisation de remplissseur sur la 1ère ligne //On aurait aussi pu utiliser nomAN puiqu'il est inutile pour la suite de cette méthode
            while ((tmp = reader.readLine()) != null) {  // while pour parcourir chaque ligne qui est stocker dans tmp en même temps que l'on vérifie que ligne non vide
                //On est obliger d'utiliser tmp car le readline fait passer à la ligne suivante.
                contenu += "\n"; //indique le saut de ligne
                contenu += tmp; //on ajoute la nouvelle ligne
            }

            reader.close(); // fermeture du BufferedReader
//            System.out.println(remplisseur);  //test de vérification
        } catch (FileNotFoundException e1) {
            System.err.println("Problème d'ouverture du fichier.");
            e1.toString();
        } catch (IOException ex) {
            System.err.println("Erreur de flux dans la lecture de la ligne");
            Logger.getLogger(AnToJson.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//useless
        }
        return contenu;
    }

    /**
     * Méthode qui traite le String retourné par "lectureAn", et écrit le
     * fichier ".json".
     *
     * @param remplisseur
     * @type String correspondant au String retourner par la méthode
     * "lectureAn", et donc au texte à traiter
     * @param nomAN
     * @type String , correspondant au nom sans son extension ".an" du fichier
     * .an à lire
     *
     * //Convention: saut de ligne via "\n" (aucun BufferedWriter.newLine();)
     * (but éviter de se perdre dans les mélanges
     */
    public static String ecritureStringJson(String remplisseur, String nomAN) {
        String gene = "";
        String coop = "";
        String fleche = "";
        ArrayList<String> l = new ArrayList<String>();  // Indispensable pour la partie flèche. Liste chaque état de chaque géne dans l'ordre. Le numéro de la ligne est indispensable pour bien positionner les flèches

        String delimiteurs = " \n\",[]=";           //initialisation du tokenizer pour parser le string ( \n pour les saut de ligne, sinon considéré comme un mot espace par le tok)
        StringTokenizer tok = new StringTokenizer(remplisseur, delimiteurs);
//============================================================================================================ NODE GENES

        gene = "{\n" + "  \"nodes\":[\n";  // écriture 2 1ères ligne d'entête ; situation: dans écriture des "nodes" (gènes) 
//writer.write("{\n" + "  \"nodes\":[\n"); 

        int nGroupe = 1;
        String nomGeneTemporaire = tok.nextToken(); //utilisé pour les différents états du géne.
        String nomPrem = nomGeneTemporaire; // utilisé pour détecter la sortie de la boucle de création des gènes, et rentrer dans la partie "links" 

        String nomProchainGene = null; //voir utilité après //obligé de l'initialiser pour éviter avertissement de netbeans; et permet de rentrer la 1ère fois dans la boucle            
        int etat = 1;
        boolean memeGroupe = true;
        while (!nomPrem.equals(nomProchainGene)) {  // faiblesse de la condition d'arrêt A AMELIORER
            gene += "    {\"name\":\"" + nomGeneTemporaire + "." + tok.nextToken() + "\",\"group\":" + nGroupe + "},\n";
            l.add(nomGeneTemporaire + ".1");   // numérote le moindre état d'un géne dans l'ordre
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + tok.nextToken() + "\",\"group\":" + nGroupe + "},\n"); //1ère ligne du groupe suivant //tok.nextToken() vaut toujours 0 ici
//                writer.write(tok.nextToken());
//                writer.write(tok.nextToken());
//                writer.write(tok.nextToken());

            memeGroupe = true; // pour re rentrer dans les boucles des états d'un autre géne.
            etat = 1;           // idem avec la bonne valeur initial de etat
            while (memeGroupe) {   // boucle pour les états d'un gène (si plus de 1 état dans le gène

                nomProchainGene = tok.nextToken();       // = numéro de l'état tant qu'il y a un état à rajouter au groupe (ou gène); sinon = au nom du prochain gène à traiter
                //    System.out.println(nomProchainGene);
                if (nomProchainGene.equals(Integer.toString(etat))) {
                    gene += "    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n";
                    l.add(nomGeneTemporaire + "." + etat);   // numérote le moindre état d'un géne dans l'ordre
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n");
//           System.out.println(tok.nextElement());
//           System.out.println(tok.nextElement());
                    etat++;
                } else {
                    memeGroupe = false;
                }
//                System.out.println("etat : " + etat);
            }
            memeGroupe = true;

            nGroupe++; // on passe au groupe suivant
            nomGeneTemporaire = nomProchainGene; //Importance de nomProchainGene, qui sera encore utilisé dans le prochain if de la boucle while pour les états.
        }
//A la sortie, "nomProchainGene" ou "nomGeneTemporaire = au nom du 1er genes avec changement d'état, suite à une frappe en coop ou pas.
//============================================================================================================ NODE COOP et flèche
        fleche = "  \"links\":[\n"; //écriture des 2 premières lignes pour la partie liens/links du fichier JSON

        boolean cooptest = false;
        while (!nomProchainGene.equals("initial_context")) {
            String etatIni = "0";    // état initial origine de la flèche 
            String etatFin = "1";    // état final extrémitée de la flèche 
            etatIni = tok.nextToken();
            if (!tok.nextToken().equals("->")) {
                System.out.println("Manque une flèche \"->\" ou problème ");
            }
            etatFin = tok.nextToken();

            int source = 0, target = 0;

            ArrayList<String> nomCoop = new ArrayList<String>(); // liste des génes coopérants pour la frappe changeant l'état de nomGeneTemporaire.
            ArrayList<String> nomEtat = new ArrayList<String>(); // liste des états des gènes de la liste précédente.
            if (!tok.nextToken().equals("when")) {
                //System.out.println("condition ");
                nomCoop.add(tok.nextToken());
                nomEtat.add(tok.nextToken());
                while ((nomProchainGene = tok.nextToken()).equals("and")) {
                    nomCoop.add(tok.nextToken());
                    nomEtat.add(tok.nextToken());
                    cooptest = true;
                }

            }
            source = nomNumero(l, nomGeneTemporaire + "." + etatIni);
            target = nomNumero(l, nomGeneTemporaire + "." + etatFin);
            fleche += " {\"source\":" + source + ",\"target\":" + target + ",\"type\":\"normal\"},";       // écriture de la fléche de saut d'état d'un gène

            
            
            while (cooptest) {   // boucle pour les états d'un gène (si plus de 1 état dans le gène

                nomProchainGene = tok.nextToken();       // = numéro de l'état tant qu'il y a un état à rajouter au groupe (ou gène); sinon = au nom du prochain gène à traiter
                //    System.out.println(nomProchainGene);
                if (nomProchainGene.equals(Integer.toString(etat))) {
                    gene += "    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n";
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n");
//           System.out.println(tok.nextElement());
//           System.out.println(tok.nextElement());
                    etat++;
                } else {
                    cooptest = false;
                }
//                System.out.println("etat : " + etat);
            }

        } //fin while de fleche. On passe à la situation initiale.

//            while (tok.hasMoreElements()) {
//                writer.write(tok.nextToken()); 
//            }
        String remplisseurFinal = gene + coop + fleche;
        return remplisseurFinal;
    }

    /**
     * Méthode final, qui crée et écrit le fichier .json (nomAN.json)
     *
     * @param remplisseur
     * @param nomAN
     */
    public static void ecritureJson(String remplisseurFinal, String nomAN) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(nomAN + ".json")); //création du fichier .json ( à remplir)
            logger.info("Ficher " + nomAN + ".json créé, et à remplir.");

            writer.write(remplisseurFinal);   // écriture

            writer.close();  // pas oublié de fermer le flux (serait mieux dans le finally
        } catch (IOException e2) {
            System.err.println("Problème d'écriture du nouveau fichier.");
            e2.printStackTrace();
        } finally {
//useless for the moment
        }
    }

    /**
     * donne juste le numéro du noeud de l'état du gène
     *
     * @param l
     * @param nom
     * @return
     */
    public static int nomNumero(ArrayList<String> l, String nom) {
        int i = 0;
        while (!l.get(i).equals(nom) && i < l.size()) {
            i++;
        }
        if (i < l.size()) {
            logger.severe("ERREUR: dans nomNumero, Dépassement de la taille de la liste!!!");
        }
        return i;
    }
}

//{
//  "nodes":[
//    {"name":"BMAL1_cyt.0","group":1},
//    {"name":"BMAL1_cyt.1","group":1},

