/**
 * Copyright 2010 - 2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.exodus.log.iterate;

import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterableBase;
import jetbrains.exodus.ByteIterator;
import jetbrains.exodus.ExodusException;
import jetbrains.exodus.log.ByteIterableWithAddress;
import org.jetbrains.annotations.NotNull;

public class FixedLengthByteIterable extends ByteIterableBase {

    private final ByteIterable source;
    private final int offset;

    public FixedLengthByteIterable(@NotNull final ByteIterable source, final int offset, final int length) {
        if (length < 0) {
            throw new ExodusException("ByteIterable length can't be less than zero");
        }
        this.source = source;
        this.offset = offset;
        this.length = length;
    }

    public FixedLengthByteIterable(@NotNull final ByteIterable source, final int length) {
        this(source, 0, length);
    }

    @SuppressWarnings({"CompareToUsesNonFinalVariable"})
    @Override
    public int compareTo(ByteIterable right) {
        if (source instanceof ByteIterableWithAddress) {
            final ByteIterableWithAddress src = (ByteIterableWithAddress) source;
            return src.compareTo(offset, length, right);
        }
        return super.compareTo(right);
    }

    @Override
    public int getLength() {
        return length;
    }

    public ByteIterable getSource() {
        return source;
    }

    @Override
    protected ByteIterator getIterator() {
        if (length == 0) {
            return ByteIterable.EMPTY_ITERATOR;
        }
        final ByteIterator bi = source.iterator();
        bi.skip(offset);
        return new ByteIterator() {
            private int i = length;

            @Override
            public boolean hasNext() {
                return i > 0 && bi.hasNext();
            }

            @Override
            public byte next() {
                i--;
                return bi.next();
            }

            @Override
            public long skip(long length) {
                long result = bi.skip(Math.min(length, i));
                i -= (int) result;
                return result;
            }
        };
    }
}
