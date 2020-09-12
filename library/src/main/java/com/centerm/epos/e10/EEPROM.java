package com.centerm.epos.e10;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class EEPROM {
	public static final int PRODUCTNAME = 0; //��Ʒ����
	public static final int PRODUCTSN = 16; //��Ʒ���к�
	public static final int PRODUCDATE = 48; //��Ʒ��������
	public static final int PRODUCTMAC1 = 56; //��Ʒmac2
	public static final int PRODUCTMAC2 = 62; //��Ʒmac2
	public static final int PRODUCTRESERVED = 68; //��ƷԤ���ֶ�
	private static final Map<Integer, Integer> productMap = new HashMap<Integer, Integer>(){
		{
			put( PRODUCTNAME, 16 );
			put( PRODUCTSN, 32 );
			put( PRODUCDATE, 8 );
			put( PRODUCTMAC1, 6 );
			put( PRODUCTMAC2, 6 );
			put( PRODUCTRESERVED, 188 );
		}
		};
	
	/*!
	 * @brief дeeprom
	 * @param infType д����Ϣ�����ͣ���ϸ������
	 * @param info д��Ϣ������
	 * @return ���ز����Ƿ�ɹ�
	 */
	public static boolean write( int infType, byte[] info )
	{
		RandomAccessFile randomAccessFile = null;
		long offset = 0;
		boolean bRet = false;
		try {
			randomAccessFile = new RandomAccessFile( "/sys/bus/i2c/devices/4-0050/eeprom", "rw");
			offset = infType;
			randomAccessFile.seek(offset);
			//��ʼ����Ҫд��ģ��
			for( int  i = 0; i < productMap.get(infType); i++ )
			{
				randomAccessFile.write( (byte)'\0' );
			}
			randomAccessFile.seek(offset); //�ع�����Ҫд����ʼλ��
			randomAccessFile.write(info); //д����
			bRet = true;
		} catch (IOException e) {
			e.printStackTrace();
			bRet = false;
		}
		finally
		{
			if( null != randomAccessFile )
			{
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return bRet;
	}
	
	/*!
	 * @brief ��eeprom
	 * @param infType ��ȡ��Ϣ���ͣ���ϸ������
	 * @return ���ض�ȡ���ֽ�
	 */
	public static String read(int infType )
	{
		RandomAccessFile randomAccessFile = null;
		long offset = 0;
		int len = productMap.get(infType);
		byte [] info = new byte[ len ]; //
		String sInfo = new String();
		try {
			randomAccessFile = new RandomAccessFile( "/sys/bus/i2c/devices/4-0050/eeprom", "r");
			offset = infType;
			randomAccessFile.seek(offset);
			randomAccessFile.read(info, 0, len ); //������
			if( -1 == info[0] ) //ȫ�����壬δ����״̬
			{
				sInfo = "";
			}
			else
			{
				sInfo = new String( info,"UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if( null != randomAccessFile )
			{
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return sInfo; 
	}

}
