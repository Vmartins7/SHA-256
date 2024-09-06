import br.com.thecode26.SHA256;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author Vanderson De O. Martins
 */
public class TestSha256 {

    private static Random random = new Random();
    private static int NUMBER_OF_TESTS = 1000;
    private static int ARBITRARY_NUMBER = 26 + 1;

    public static void main(String[] args) throws IOException {

        Path path;
        if(args.length == 1){
            path = Paths.get(args[0]);
        }else{
            path = Paths.get("NIST.FIPS.180-4.pdf");
        }

        if(Files.exists(path)){
            InputStream is = new FileInputStream(path.toFile());
            SHA256 mySha256 = new SHA256();
            System.out.println("Hashing file " + path + " :");
            String hashFromMaybeSpecFile = bytesToHex(mySha256.digestum(is));
            System.out.println(hashFromMaybeSpecFile);
            if(!hashFromMaybeSpecFile.equals("0455b406d89648d20cbde375561e19c245b9815e894164c2670772e3d54deb82") && args.length == 0){
                printAndExit("Incorrect hash value.");
            }
        }

        for(int i = 0; i < NUMBER_OF_TESTS; i++){
            String generatedMsg = generateRandomText();
            if(!singleTest(generatedMsg)){
                printAndExit("Incorrect hash value.");
            }
        }
        System.out.println("\nAll tests passed");
    }


    private static boolean singleTest(String message){
        //System.out.println(message);
        SHA256 mySha256 = new SHA256();
        String mySha = " ", referenceSha = "";
        try{
            mySha = bytesToHex(mySha256.digestum(from(message)));
            referenceSha = obtainCommonHash(from(message));
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return mySha.equals(referenceSha);
    }


    private static String obtainCommonHash(InputStream inputStream) {
        MessageDigest digest = null;
        byte[] hash = null;
        try{
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(
                    inputStream.readAllBytes()
            );
        }catch (NoSuchAlgorithmException | IOException e){
            e.printStackTrace();
        }

        return bytesToHex(hash);
    }


    private static InputStream from(String str){
        return new ByteArrayInputStream(str.getBytes());
    }


    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    private static String bytesToHex(int[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = String.format("%8s",Integer.toHexString(0xffffffff & hash[i])).replace(' ','0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    private static String generateRandomText(){
        final int minSize = ARBITRARY_NUMBER;
        final int maxSize = 2048 + ARBITRARY_NUMBER;
        int size = random.nextInt(maxSize + 1) + minSize;

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < size; i++){
            stringBuilder.append((char) randomizeNextPositive(65,90));
        }
        return stringBuilder.toString();
    }

    static int randomizeNextPositive(int start, int inclusiveLimit){
        int value = random.nextInt();
        if(value < 0){
            value &= 0x7FFFFFFF;
        }
        value = value % ((inclusiveLimit - start) + 1) + start;
        return value;
    }

    private static void printAndExit(String msg){
        System.err.println(msg);
        System.exit(1);
    }



}