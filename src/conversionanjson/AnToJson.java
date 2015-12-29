/*
 * Classe lisant le ficher .an et creéant le fichier .json correspondant
 * avec automatiquement le même nom (seul l'extension change.)
 *
 * Le nom du fichier .an à lire est rentré en paramétre, sans son extension .an. 
 */
package conversionanjson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
        ecritureJson(ecritureStringJson(lectureAn(nomAN), nomAN), nomAN);
    }

    /**
     * Méthode static destiné à être appeler par la méthode principale "methode"
     * de la classe AnToJson
     *
     * @param nomAN type String, correspondant au nom sans son extension ".an"
     * du fichier .an à lire
     * @return remplisseur type String, correspondant exactement au texte du
     * fichier .an
     */
    private static String lectureAn(String nomAN) {
        String remplisseur = null; // null parceque sinon netbeans détecte qu'il n'a potentiellement pas été initialisé....
        String tmp; // pour stocker la ligne courante en même temps qu'on fait le test
        //tmp est initialisé et changer dans la condition du while. Il y a surement mieux à faire.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(nomAN + ".an")); //ouverture du fichier à lire
            logger.info("Ficher " + nomAN + ".an ouvert.");

            remplisseur = reader.readLine();   // initialisation de remplissseur sur la 1ère ligne //On aurait aussi pu utiliser nomAN puiqu'il est inutile pour la suite de cette méthode
            while ((tmp = reader.readLine()) != null) {  // while pour parcourir chaque ligne qui est stocker dans tmp en même temps que l'on vérifie que ligne non vide
                //On est obliger d'utiliser tmp car le readline fait passer à la ligne suivante.
                remplisseur += "\n"; //indique le saut de ligne
                remplisseur += tmp; //on ajoute la nouvelle ligne
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
        return remplisseur;
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
     */
    public static String ecritureStringJson(String remplisseur, String nomAN) {
String gene="";
String coop="";
String fleche="";
        

            String delimiteurs = " \n\",[]";           //initialisation du tokenizer pour parser le string ( \n pour les saut de ligne, sinon considéré comme un mot espace par le tok)
            StringTokenizer tok = new StringTokenizer(remplisseur, delimiteurs);
//============================================================================================================ NODE GENES
            //Convention: saut de ligne via "\n" (aucun BufferedWriter.newLine();) (but éviter de se perdre dans les mélanges 
gene="{\n" + "  \"nodes\":[\n";            
//writer.write("{\n" + "  \"nodes\":[\n"); // écriture 2 1ères ligne d'entête ; situation: dans écriture des "nodes" (gènes) 

            int nGroupe = 1;
            String nomGeneTemporaire = tok.nextToken(); //utilisé pour les différents états du géne.
            String nomPrem = nomGeneTemporaire; // utilisé pour détecter la sortie de la boucle de création des gènes, et rentrer dans la partie "links" 

            String nomProchainGene = null; //voir utilité après //obligé de l'initialiser pour éviter avertissement de netbeans.            
            while (!nomPrem.equals(nomProchainGene)) {
gene+="    {\"name\":\"" + nomGeneTemporaire + "." + tok.nextToken() + "\",\"group\":" + nGroupe + "},\n";
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + tok.nextToken() + "\",\"group\":" + nGroupe + "},\n"); //1ère ligne du groupe suivant //tok.nextToken() vaut toujours 0 ici
//                writer.write(tok.nextToken());
//                writer.write(tok.nextToken());
//                writer.write(tok.nextToken());

                boolean memeGroupe = true;

                while (memeGroupe) {   // boucle pour les états d'un gène (si plus de 1 état dans le gène
                    int etat = 1;
                    nomProchainGene = tok.nextToken();       // = numéro de l'état tant qu'il y a un état à rajouter au groupe (ou gène); sinon = au nom du prochain gène à traiter
                    System.out.println(nomProchainGene);
                    if (nomProchainGene.equals(Integer.toString(etat))) {
                        gene+="    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n";
//writer.write("    {\"name\":\"" + nomGeneTemporaire + "." + etat + "\",\"group\":" + nGroupe + "},\n");
//           System.out.println(tok.nextElement());
//           System.out.println(tok.nextElement());
                        etat++;
                    } else {
                        memeGroupe = false;
                    }
//                System.out.println("etat : " + etat);
                }
                memeGroupe = true; // pour re rentrer dans les boucles des états d'un autre géne.

                nGroupe++; // on passe au groupe suivant
                nomGeneTemporaire = nomProchainGene; //Importance de nomProchainGene, qui sera encore utilisé dans le prochain if de la boucle while pour les états.
            }
//============================================================================================================ NODE COOP

//            while (tok.hasMoreElements()) {
//                writer.write(tok.nextToken()); 
//            }
  String remplisseurFinal = gene+coop+fleche;  
return remplisseurFinal;
    }
/**
 * Méthode final, qui crée et écrit le fichier .json (nomAN.json)
 * @param remplisseur
 * @param nomAN 
 */
    public static void ecritureJson(String remplisseurFinal, String nomAN) {
                try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(nomAN + ".json")); //création du fichier .json ( à remplir)
            logger.info("Ficher " + nomAN + ".json créé, et à remplir.");
        writer.close();  // pas oublié de fermer le flux (serait mieux dans le finally
        } catch (IOException e2) {
            System.err.println("Problème d'écriture du nouveau fichier.");
            e2.toString();
        } finally {
//useless for the moment
        }
    }
}

//{
//  "nodes":[
//    {"name":"BMAL1_cyt.0","group":1},
//    {"name":"BMAL1_cyt.1","group":1},
