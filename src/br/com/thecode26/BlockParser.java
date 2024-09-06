package br.com.thecode26;

import java.io.IOException;
import java.io.InputStream;

import static br.com.thecode26.Constants.*;

/**
 * @author Vanderson De O. Martins
 */
public class BlockParser {

    private byte[] buffer;
    private InputStream is;
    private long messageLength;
    private boolean noMoreInputBlocks;
    byte[] additionalBlock;

    public BlockParser(InputStream inputStream) {
        this.is = inputStream;
        this.buffer = new byte[BLOCK_IB];
        this.additionalBlock = new byte[BLOCK_IB];
    }


    public byte[] getBlock() throws IOException{
        final int EOF = -1;
        int nbr = 0;
        int flag;

        if(noMoreInputBlocks){
            if(additionalBlock != null){
                byte[] temp = additionalBlock;
                additionalBlock = null;
                return temp;
            }
            return null;
        }

        do{
            flag = is.read(buffer, nbr, buffer.length - nbr);
            if(flag != EOF){
                nbr += flag;
            }else{
                noMoreInputBlocks = true;
                break;
            }
        }while(nbr != buffer.length);

        this.messageLength += (nbr * Byte.SIZE);
        if(noMoreInputBlocks){
            setAdditionalInfo(buffer, nbr);
        }

        return this.buffer;
    }


    private void setAdditionalInfo(byte[] buffer, int nbr){
        if(nbr < buffer.length - MLM_IB){
            this.additionalBlock = null;
            buffer[nbr] = (byte) (1 << Byte.SIZE - 1);
            fillWithPadding(buffer, nbr + 1, buffer.length - MLM_IB);
            appendLengthMessage(buffer);
        }else if(nbr >= buffer.length - MLM_IB && nbr < buffer.length){
            buffer[nbr] = (byte) (1 << Byte.SIZE - 1);
            fillWithPadding(buffer, nbr + 1, buffer.length);
            fillWithPadding(additionalBlock,0, buffer.length - MLM_IB);
            appendLengthMessage(additionalBlock);
        }else if(nbr == buffer.length){
            additionalBlock[0] = (byte) (1 << Byte.SIZE - 1);
            fillWithPadding(additionalBlock,1, buffer.length - MLM_IB);
            appendLengthMessage(additionalBlock);
        }
    }


    private void fillWithPadding(byte[] block, int filledBytes, int limit){
        for(int i = filledBytes; i < limit; i++){
            block[i] = 0;
        }
    }


    private void appendLengthMessage(byte[] buffer){

        for(int i = buffer.length; i != buffer.length - MLM_IB;){
            --i;
            buffer[i] = (byte) (messageLength & 0b1111_1111);
            messageLength >>>= Byte.SIZE;
        }
    }



}
