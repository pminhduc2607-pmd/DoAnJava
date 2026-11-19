CREATE DATABASE QLQuanCaPhe;
GO
USE QLQuanCaPhe;
GO
 
CREATE TABLE LoaiMon (
    MaLoai VARCHAR(10) PRIMARY KEY,
    TenLoai NVARCHAR(100) NOT NULL,
    MoTa NVARCHAR(255)
);
 
CREATE TABLE Mon (
    MaMon VARCHAR(10) PRIMARY KEY,
    TenMon NVARCHAR(150) NOT NULL,
    DonViTinh NVARCHAR(30) NOT NULL,
    DonGia FLOAT NOT NULL CHECK (DonGia >= 0),
    MaLoai VARCHAR(10) NOT NULL,
    CONSTRAINT FK_Mon_LoaiMon FOREIGN KEY (MaLoai)
        REFERENCES LoaiMon(MaLoai)
);
 
CREATE TABLE Ban (
    MaBan VARCHAR(10) PRIMARY KEY,
    TenBan NVARCHAR(50) NOT NULL,
    TrangThai NVARCHAR(20) NOT NULL DEFAULT N'Trống'
);
 
CREATE TABLE NhanVien (
    MaNV VARCHAR(10) PRIMARY KEY,
    HoTen NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15) NOT NULL,
    ChucVu NVARCHAR(50) NOT NULL,
    MatKhau VARCHAR(50) NOT NULL
);
 
CREATE TABLE HoaDon (
    MaHD VARCHAR(10) PRIMARY KEY,
    NgayLap DATE NOT NULL DEFAULT GETDATE(),
    MaNV VARCHAR(10) NOT NULL,
    MaBan VARCHAR(10) NOT NULL,
    TongTien FLOAT DEFAULT 0,
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (MaNV)
        REFERENCES NhanVien(MaNV),
    CONSTRAINT FK_HoaDon_Ban FOREIGN KEY (MaBan)
        REFERENCES Ban(MaBan)
);
 
CREATE TABLE ChiTietHoaDon (
    MaHD VARCHAR(10) NOT NULL,
    MaMon VARCHAR(10) NOT NULL,
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    DonGia FLOAT NOT NULL CHECK (DonGia >= 0),
    ThanhTien AS (SoLuong * DonGia),
    PRIMARY KEY (MaHD, MaMon),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (MaHD)
        REFERENCES HoaDon(MaHD) ON DELETE CASCADE,
    CONSTRAINT FK_CTHD_Mon FOREIGN KEY (MaMon)
        REFERENCES Mon(MaMon)
);
GO
 
INSERT INTO LoaiMon VALUES
('L01', N'Cà phê', N'Cà phê đen, cà phê sữa, bạc xỉu'),
('L02', N'Trà & Trà sữa', N'Trà đào, trà sữa trân châu'),
('L03', N'Sinh tố & Nước ép', N'Sinh tố bơ, nước cam ép'),
('L04', N'Bánh ngọt', N'Bánh tiramisu, bánh flan');
 
INSERT INTO Mon VALUES
('M01', N'Cà phê đen', N'Ly', 20000, 'L01'),
('M02', N'Cà phê sữa', N'Ly', 25000, 'L01'),
('M03', N'Bạc xỉu', N'Ly', 29000, 'L01'),
('M04', N'Trà đào cam sả', N'Ly', 35000, 'L02'),
('M05', N'Trà sữa trân châu', N'Ly', 32000, 'L02'),
('M06', N'Sinh tố bơ', N'Ly', 40000, 'L03'),
('M07', N'Bánh tiramisu', N'Cái', 38000, 'L04');
 
INSERT INTO Ban VALUES
('B01', N'Bàn 1', N'Trống'),
('B02', N'Bàn 2', N'Có khách'),
('B03', N'Bàn 3', N'Trống'),
('B04', N'Bàn 4', N'Trống');
 
INSERT INTO NhanVien VALUES
('NV01', N'Nguyễn Văn An', '0901234567', N'Quản lý', '123456'),
('NV02', N'Trần Thị Bình', '0912345678', N'Thu ngân', '123456'),
('NV03', N'Lê Hoàng Nam', '0987654321', N'Pha chế', '123456');
 
INSERT INTO HoaDon VALUES
('HD01', '2026-10-28', 'NV02', 'B01', 85000),
('HD02', '2026-10-29', 'NV02', 'B03', 102000),
('HD03', '2026-10-30', 'NV02', 'B02', 78000);
 
INSERT INTO ChiTietHoaDon (MaHD, MaMon, SoLuong, DonGia) VALUES
('HD01', 'M02', 2, 25000),
('HD01', 'M04', 1, 35000),
('HD02', 'M05', 2, 32000),
('HD02', 'M07', 1, 38000),
('HD03', 'M01', 1, 20000),
('HD03', 'M03', 2, 29000);
GO
