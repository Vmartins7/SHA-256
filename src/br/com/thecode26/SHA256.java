package br.com.thecode26;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import static br.com.thecode26.Constants.*;

/**
 * @author Vanderson De O. Martins
 */
public class SHA256 {

    private int[] currentHash;
    private int[] schedl;
    private byte[] block;
    
    private int[] wvs;

    private static Byte A=0, B=1, C=2, D=3, E=4, F=5, G=6, H=7;

    public SHA256(){
        currentHash = new int[HSH_I.length];
        System.arraycopy(HSH_I,0,currentHash,0,HSH_I.length);
        schedl = new int[M_SCHEDULE_IW];
        wvs = new int[currentHash.length];
    }


    public int[] digestum(InputStream is) throws IOException{
        int t1, t2;
        Objects.requireNonNull(is);
        BlockParser blockParser = new BlockParser(is);

        while((block = blockParser.getBlock()) != null)
        {
            prepareMessageSchedule(block);
            System.arraycopy(currentHash,0, wvs,0, currentHash.length);
            for(int t = 0; t < M_SCHEDULE_IW; t++){
                t1 = wvs[H] + sigma1(wvs[E]) + choose(wvs[E], wvs[F], wvs[G]) + KAPPAS[t] + schedl[t];
                t2 = sigma0(wvs[A]) + majority(wvs[A], wvs[B], wvs[C]);
                wvs[H] = wvs[G];
                wvs[G] = wvs[F];
                wvs[F] = wvs[E];
                wvs[E] = wvs[D] + t1;
                wvs[D] = wvs[C];
                wvs[C] = wvs[B];
                wvs[B] = wvs[A];
                wvs[A] = t1 + t2;
            }
            for(int i = 0; i < currentHash.length; i++){
                currentHash[i] = currentHash[i] + wvs[i];
            }
        }

        return this.currentHash;
    }



    private void prepareMessageSchedule(byte[] block){
        int i = 0;
        for(;i < WORDS_PER_BLOCK; i++){
            schedl[i] = cast(block, i * WORD_SIZE_IB);
        }
        for(; i < M_SCHEDULE_IW; i++){
            schedl[i] = sigmaMinor1(schedl[i - 2]) + schedl[i - 7] + sigmaMinor0(schedl[i - 15]) + schedl[i - 16];
        }
    }

    int cast(byte[] pBlock, int start){
        int word = 0;
        for(int i = start; i < start + WORD_SIZE_IB; i++){
            word |= (pBlock[i] & 0xFF);
            if(i != start + WORD_SIZE_IB -1){
                word <<= Byte.SIZE;
            }
        }
        return word;
    }

    private int rotRh(int word, int n){
        return (word >>> n) | (word << (Integer.SIZE - n));
    }

    private int sigma0(int word){
        return rotRh(word,2) ^ rotRh(word, 13) ^ rotRh(word, 22);
    }

    private int sigma1(int word){
        return rotRh(word,6) ^ rotRh(word, 11) ^ rotRh(word, 25);
    }

    private int sigmaMinor0(int word){
        return rotRh(word,7) ^ rotRh(word, 18) ^ (word >>> 3);
    }

    private int sigmaMinor1(int word){
        return rotRh(word,17) ^ rotRh(word, 19) ^ (word >>> 10);
    }

    private int choose(int wx, int wy, int wz){
        return (wx & wy) ^ (~wx & wz);
    }

    private int majority(int wx, int wy, int wz){
        return (wx & wy) ^ (wx & wz) ^ (wy & wz);
    }




}
