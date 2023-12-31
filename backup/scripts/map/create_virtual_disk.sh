#virtual box super user
sudo adduser $USER vboxusers

fallocate -l 8000M Virtual_usb.img
mkfs -t ext4 Virtual_usb.img
sudo mkdir /media/usb_mount_point
sudo mount -t auto -o loop Virtual_usb.img /media/usb_mount_point
#it will change permission on the real image as it's mounted
sudo chown raxim /media/usb_mount_point
sudo chmod -R g+rw /media/usb_mount_point

