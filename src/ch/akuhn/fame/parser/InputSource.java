//  Copyright (c) 2007-2008 Adrian Kuhn <akuhn(a)iam.unibe.ch>
//  
//  This file is part of 'Fame (for Java)'.
//  
//  'Fame (for Java)' is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or (at your
//  option) any later version.
//  
//  'Fame (for Java)' is distributed in the hope that it will be useful, but
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
//  License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public License
//  along with 'Fame (for Java)'. If not, see <http://www.gnu.org/licenses/>.
//  

package ch.akuhn.fame.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

import ch.akuhn.util.Throw;

public class InputSource implements Iterator<Character>, Iterable<Character> {

    public static final char EOF = (char) -1;

    public static InputSource fromFile(File file) {
        try {
            /*
             * There is one specialized form of direct ByteBuffer known as a
             * MappedByteBuffer. This class represents a buffer of bytes mapped
             * to a file. To map a file to a MappedByteBuffer, you first must
             * get the channel for a file. A channel represents a connection to
             * something, such as a pipe, socket, or file, that can perform I/O
             * operations. In the case of a FileChannel, you can get one from a
             * FileInputStream, FileOutputStream, or RandomAccessFile through
             * the getChannel method. Once you have the channel, you map it to a
             * buffer with map, specifying the mode and portion of the file you
             * want to map. The file channel can be opened with one of the
             * FileChannel.MapMode constants: read-only (READ_ONLY),
             * private/copy-on-write (PRIVATE), or read-write (READ_WRITE).
             */

            FileInputStream input = new FileInputStream(file);
            FileChannel channel = input.getChannel();
            long fileLength = channel.size();
            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_ONLY, 0, fileLength);

            /*
             * If you need to treat the bytes as characters, you must convert
             * the ByteBuffer into a CharBuffer through the use of a character
             * set for the conversion. This character set is specified by the
             * Charset class. You then decode the file contents through the
             * CharsetDecoder class. There is also a CharsetEncoder to go in the
             * other direction.
             */

            Charset charset = Charset.forName("ISO-8859-1");
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(buffer);

            return fromString(charBuffer);
        } catch (Exception ex) {
            throw Throw.exception(ex);
        }
    }

    public static InputSource fromFilename(String filename) {
        return fromFile(new File(filename));
    }

    public static InputSource fromInputStream(InputStream stream) {
        StringBuilder buf = new StringBuilder();
        try {
            while (true) {
                int ch = stream.read();
                if (ch < 0) break;
                buf.append((char) ch);
            }
            stream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return fromString(buf);
    }

    public static InputSource fromResource(String name) {
        InputStream in = ClassLoader.getSystemResourceAsStream(name);
        return fromInputStream(in);
    }

    public static InputSource fromString(CharSequence string) {
        return new InputSource(string);
    }

    private int index;
    private int start;
    private final int length;
    private final CharSequence string;

    private int line;
    private int prevLineBreak;

    private InputSource(CharSequence string) {
        index = 0;
        start = -1;
        line = 1;
        prevLineBreak = -1;
        length = string.length();
        this.string = string;
    }

    public Position getPosition() {
        return new Position(line, index - prevLineBreak, index);
    }

    public boolean hasNext() {
        return this.peek() != EOF;
    }

    public final void inc() {
        index++;
    }

    public final void inc2() { // TODO nicer name
        if (string.charAt(index) == '\n') {
            prevLineBreak = index;
            line++;
        }
        index++;
    }

    public Iterator<Character> iterator() {
        this.rewind();
        return this;
    }

    public void mark() {
        start = index;
    }

    public Character next() {
        this.inc();
        return this.peek();
    }

    public final char peek() {
        return index < length ? string.charAt(index) : EOF;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public final void rewind() {
        index = 0;
        start = -1;
    }

    public final CharSequence yank() {
        return string.subSequence(start, index);
    }

}
