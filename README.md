# network_sdk
网络通信

# UsbDeviceManager：设置USB网络共享，系统修改在下面

# SocketConnect：建立socket连接

# 


#设置USB网络共享主要思路
Android端
1.
根据AndroidOS/device/lge/bullhead/init.bullhead.usb.rc文件的属性值切换usb的不同状态。状态如下
ptp,adb//usb传照片（ptp）
mtp,adb//usb传文件
midi,adb//usb用作MIDI设备
mtp,adb//usb为设备充电 		
rndis,adb//usb网络共享

切换命令如下：
persist.sys.usb.config,sys.usb.config
setprop sys.usb.config  rndis,adb
getprop sys.usb.config
2.
根据1的命令切换usb状态为“usb网络共享”。之后执行ifconfig rndis0 192.168.42.129 up启用网卡，ip rule add from all lookup main pref 0配置路由。
Pc端 
1.配置网络适配器的ip等为android端2步骤对应的地址。例如192.168.42.2

经过以上两步网络之后UBS共享网络就配置好了。可以通过TCP进行通信。
如果要用代码启用usb共享网络，请继续以下步骤。
Android端
1.
这一步和上面一样，执行setprop设置属性值
2.根据init.rc语法创建Service服务，用来执行上面root权限执行的命令。服务的启动条件为1步骤中改变的属性值。（具体参考init.rc脚本的Action、Commands、Services、Options）
----------------------------------------------------------------------------

修改的所有文件
1.添加文件AndroidOS/device/mediatek/common/rndis.sh，功能为执行Android端第二部的命令
#!/system/bin/sh
searchNetworkCard="rndis"
networkCardInfo=0
ipAddress=0
i=1

#1 find network card
while(( $i<=200 ))
do
	networkCardInfo=`ifconfig -a | grep $searchNetworkCard | cut -d ' ' -f 1`
	if [ -n "$networkCardInfo" ]
	then
		searchNetworkCard=$networkCardInfo
		break;
	else

		let "i++"
		sleep 0.01
	fi
done

if [ -n "$searchNetworkCard " ]
then
	#2 find ip address
	ipAddress=`ifconfig $searchNetworkCard | grep 'inet addr' | cut -d ':' -f 2 | cut -d ' ' -f 1`
	#3 set address
	if [ $ipAddress!="192.168.7.2" ]
	then
	   ifconfig $searchNetworkCard 192.168.7.2 up
	   ip rule add from all lookup main pref 0
	fi
Fi
2.AndroidOS/device/mediatek/common/device.mk文件添
加“PRODUCT_COPY_FILES += device/mediatek/common/rndis.sh:system/etc/rndis.sh”把rndis.sh脚本拷贝到系统目录下
3.AndroidOS/system/core/libcutils/fs_config.c添加“{ 00755, AID_ROOT,      AID_ROOT,      0, "system/etc/rndis.sh" },”为拷贝的文件添加执行权限
4.创建service与action用来执行脚本
4.1AndroidOS/system/core/rootdir/init.rc添加
service rndis_ip /system/etc/rndis.sh
    class main
    user root
    group root
    disable
    oneshot

on property:sys.usb.config=rndis
    start rndis_ip
on property:sys.usb.config=rndis,adb
start rndis_ip

4.2 service的sepolicy
4.2.1 AndroidOS/system/sepolicy/file_contexts添加
/system/etc/rndis.sh u:object_r:rndis_exec:s0
4.2.2在同级目录下添加rndis.te文件
# service flash_recovery in init.rc
type rndis, domain, domain_deprecated;
type rndis_exec, exec_type, file_type;

init_daemon_domain(rndis)

allow rndis self:capability dac_override;

# /system/etc/rndis.sh is a shell script.
# Needs to execute /system/bin/sh
allow rndis shell_exec:file rx_file_perms;

# Execute /system/bin/ifconfig
allow rndis system_file:file rx_file_perms;

allow rndis toolbox_exec:file rx_file_perms;

# Update the recovery block device based off a diff of the boot block device
allow rndis block_device:dir search;
allow rndis boot_block_device:blk_file r_file_perms;
4.2.3
Android/system/sepolicy/shell.te添加
Android/system/sepolicy/shell.te

