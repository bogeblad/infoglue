/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.deliver.util.graphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

public class GifEncoder
{

    String b;
    int g;
    int a;
    int h;
    int f;
    int c[];
    int i[];
    int e[];
    TreeSet d;

    private void a(int ai[], int j, DataOutputStream dataoutputstream) throws Exception
    {
        try
        {
            boolean flag2 = false;
            int l;
            int j3 = (1 << (l = j + 1)) - 1;
            int i2 = (1 << j) + 2;
            byte abyte0[] = new byte[255];
            int ai1[] = new int[4096];
            int ai2[] = new int[4096];
            int ai3[] = new int[4096];
            int ai4[] = new int[i2];
            int k;
            for(k = 0; k < i2; k++)
            {
                ai4[k] = 0xffffffff | k;
                ai3[k] = -1;
            }

            for(; k < 4096; k++)
            {
                ai3[k] = -1;
            }

            System.arraycopy(ai3, 0, ai1, 0, 4096);
            System.arraycopy(ai3, 0, ai2, 0, 4096);
            System.arraycopy(ai4, 0, ai1, 0, i2);
            int j1 = ai[0];
            k = 1;
            boolean flag1 = false;
            int j2 = 0;
            int k2 = (1 << l) - 1;
            boolean flag = true;
            int i3 = 0;
            int i1 = 0;
            j2 |= 1 << j + i3;
            for(i3 += l; i3 >= 8;)
            {
                try
                {
                    abyte0[i1++] = (byte)j2;
                }
                catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception)
                {
                    dataoutputstream.writeByte(255);
                    dataoutputstream.write(abyte0);
                    abyte0[i1 = 0] = (byte)j2;
                    i1++;
                }
                i3 -= 8;
                j2 >>= 8;
            }

            try
            {
                do
                {
                    int k1;
                    int l1 = j1 << 16 | (k1 = ai[k++]);
                    int k3;
                    for(k3 = j1; ai1[k3] != l1 && ai2[k3] >= 0; k3 = ai2[k3]) { }
                    if(ai1[k3] != l1)
                    {
                        j2 |= j1 << i3;
                        for(i3 += l; i3 >= 8;)
                        {
                            try
                            {
                                abyte0[i1++] = (byte)j2;
                            }
                            catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception1)
                            {
                                dataoutputstream.writeByte(255);
                                dataoutputstream.write(abyte0);
                                abyte0[i1 = 0] = (byte)j2;
                                i1++;
                            }
                            i3 -= 8;
                            j2 >>= 8;
                        }

                        if(i2 > j3)
                        {
                            l++;
                            j3 = (j3 << 1) + 1;
                        }
                        try
                        {
                            ai2[k3] = i2;
                            ai1[i2++] = j1 << 16 | k1;
                            j1 = k1;
                        }
                        catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception2)
                        {
                            j1 = k1;
                            l--;
                            j2 |= 1 << j + i3;
                            for(i3 += l; i3 >= 8;)
                            {
                                try
                                {
                                    abyte0[i1++] = (byte)j2;
                                }
                                catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception5)
                                {
                                    dataoutputstream.writeByte(255);
                                    dataoutputstream.write(abyte0);
                                    abyte0[i1 = 0] = (byte)j2;
                                    i1++;
                                }
                                i3 -= 8;
                                j2 >>= 8;
                            }

                            j3 = (1 << (l = j + 1)) - 1;
                            i2 = (1 << j) + 2;
                            int l2 = (1 << l) - 1;
                            System.arraycopy(ai3, 0, ai1, 0, 4096);
                            System.arraycopy(ai3, 0, ai2, 0, 4096);
                            System.arraycopy(ai4, 0, ai1, 0, i2);
                        }
                    }
                    else
                    {
                        j1 = k3;
                    }
                }
                while(true);
            }
            catch(Exception exception)
            {
                j2 |= j1 << i3;
            }
            for(i3 += l; i3 >= 8;)
            {
                try
                {
                    abyte0[i1++] = (byte)j2;
                }
                catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception3)
                {
                    dataoutputstream.writeByte(255);
                    dataoutputstream.write(abyte0);
                    abyte0[i1 = 0] = (byte)j2;
                    i1++;
                }
                i3 -= 8;
                j2 >>= 8;
            }

            j2 |= (1 << j) + 1 << i3;
            for(i3 += l; i3 > 0;)
            {
                try
                {
                    abyte0[i1++] = (byte)j2;
                }
                catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception4)
                {
                    dataoutputstream.writeByte(255);
                    dataoutputstream.write(abyte0);
                    abyte0[i1 = 0] = (byte)j2;
                    i1++;
                }
                i3 -= 8;
                j2 >>= 8;
            }

            dataoutputstream.writeByte(i1);
            dataoutputstream.write(abyte0, 0, i1);
            dataoutputstream.writeByte(0);
            return;
        }
        catch(Exception e) { }
    }

    public void addTransparentColor(Color color)
    {
        try
        {
            if(f < 256)
            {
                c[f++] = color.getRGB();
            }
            return;
        }
        catch(Exception e) { }
    }

    public void setTransparentColors(Vector vector)
    {
        try
        {
            Iterator iterator = vector.iterator();
            while(iterator.hasNext()) 
            {
                Color color = (Color)iterator.next();
                addTransparentColor(color);
            }

            return;
        }
        catch(Exception e) { }
    }

    public void encode(BufferedImage bufferedimage, DataOutputStream dataoutputstream, Hashtable hashtable) throws Exception
    {
        try
        {
            a = bufferedimage.getWidth();
            g = bufferedimage.getHeight();
            e = bufferedimage.getRGB(0, 0, a, g, null, 0, a);
            int i4 = 0;
            b = hashtable.get("encoding").toString();
            if(b.equals("websafe"))
            {
                int ai[] = new int[256];
                i = new int[256];
                h = 8;
                int k1 = 0;
                int j;
                int j1 = j = 0;
                for(; j <= 255; j += 51)
                {
                    for(int l = 0; l <= 255; l += 51)
                    {
                        for(int i1 = 0; i1 <= 255;)
                        {
                            i[j1] = (j << 16) + (l << 8) + i1;
                            ai[k1++] = j1;
                            i1 += 51;
                            j1++;
                        }

                    }

                }

                if(f > 0)
                {
                    int j4 = c[0];
                    int l1 = ((c[0] >> 16 & 0xff) + 25) / 51;
                    int k2 = ((c[0] >> 8 & 0xff) + 25) / 51;
                    int j3 = ((c[0] & 0xff) + 25) / 51;
                    i4 = l1 * 36 + k2 * 6 + j3;
                    for(j = 1; j < f; j++)
                    {
                        int i2 = ((c[j] >> 16 & 0xff) + 25) / 51;
                        int l2 = ((c[j] >> 8 & 0xff) + 25) / 51;
                        int k3 = ((c[j] & 0xff) + 25) / 51;
                        ai[i2 * 36 + l2 * 6 + k3] = i4;
                    }

                }
                j = 0;
                try
                {
                    do
                    {
                        int i5 = e[j];
                        int j2 = ((i5 >> 16 & 0xff) + 25) / 51;
                        int i3 = ((i5 >> 8 & 0xff) + 25) / 51;
                        int l3 = ((i5 & 0xff) + 25) / 51;
                        e[j++] = ai[j2 * 36 + i3 * 6 + l3];
                    }
                    while(true);
                }
                catch(Exception exception1) { }
            }
            /*else
            if(b.equals("optimized"))
            {
                try
                {
                    int k4 = Integer.parseInt(hashtable.get("colors").toString());
                    for(h = 1; k4 - 1 >> h > 0; h++) { }
                    i = new int[1 << h];
                    CSelectiveQuant cselectivequant = new CSelectiveQuant();
                    for(int j5 = 0; j5 < e.length; j5++)
                    {
                        cselectivequant.addPixel(e[j5]);
                    }

                    boolean flag = f > 0;
                    int k5 = flag ? 1 : 0;
                    int ai1[] = cselectivequant.createPalette(k4 - k5);
                    for(int l5 = 0; l5 < i.length; l5++)
                    {
                        try
                        {
                            i[l5] = ai1[l5 - k5];
                        }
                        catch(ArrayIndexOutOfBoundsException arrayindexoutofboundsexception)
                        {
                            i[l5] = 0;
                        }
                    }

                    if(flag)
                    {
                        i4 = 0;
                        for(int i6 = 0; i6 < f; i6++)
                        {
                            cselectivequant.setIndex(c[i6], -1);
                        }

                    }
                    for(int j6 = 0; j6 < e.length; j6++)
                    {
                        e[j6] = cselectivequant.getIndex(e[j6]) + k5;
                    }

                }
                catch(NumberFormatException numberformatexception)
                {
                    logger.info("Parameter: 'colors' is malformated...");
                    return;
                }
            }
            */
            dataoutputstream.write("GIF89a".getBytes());
            dataoutputstream.writeByte(a);
            dataoutputstream.writeByte(a >> 8);
            dataoutputstream.writeByte(g);
            dataoutputstream.writeByte(g >> 8);
            dataoutputstream.writeByte(0xf0 | h - 1);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeByte(0);
            int k = 0;
            try
            {
                do
                {
                    int l4 = i[k++];
                    dataoutputstream.writeByte(l4 >> 16 & 0xff);
                    dataoutputstream.writeByte(l4 >> 8 & 0xff);
                    dataoutputstream.writeByte(l4 & 0xff);
                }
                while(true);
            }
            catch(Exception exception) { }
            if(f > 0)
            {
                dataoutputstream.writeByte(33);
                dataoutputstream.writeByte(249);
                dataoutputstream.writeByte(4);
                dataoutputstream.writeByte(1);
                dataoutputstream.writeByte(0);
                dataoutputstream.writeByte(0);
                dataoutputstream.writeByte(i4);
                dataoutputstream.writeByte(0);
            }
            dataoutputstream.writeByte(44);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeByte(a);
            dataoutputstream.writeByte(a >> 8);
            dataoutputstream.writeByte(g);
            dataoutputstream.writeByte(g >> 8);
            dataoutputstream.writeByte(0);
            dataoutputstream.writeByte(h);
            a(e, h, dataoutputstream);
            dataoutputstream.writeByte(59);
            dataoutputstream.flush();
            return;
        }
        catch(Exception e) { }
    }

    public GifEncoder()
    {
        f = 0;
        c = new int[256];
    }
}
