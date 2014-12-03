-- phpMyAdmin SQL Dump
-- version 4.2.8
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 08, 2014 at 07:13 PM
-- Server version: 5.6.20
-- PHP Version: 5.4.24


--
-- Database: `project4`
--

-- --------------------------------------------------------

--
-- Table structure for table `Course`
--

CREATE TABLE IF NOT EXISTS `Course` (
  `crsCode` varchar(40) NOT NULL,
  `deptId` text NOT NULL,
  `crsName` text NOT NULL,
  `descr` text NOT NULL,
  PRIMARY KEY (`crsCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Professor`
--

CREATE TABLE IF NOT EXISTS `Professor` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `deptId` text NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Student`
--

CREATE TABLE IF NOT EXISTS `Student` (
  `id` int(11) NOT NULL,
  `name` text NOT NULL,
  `address` text NOT NULL,
  `status` text NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Teaching`
--

CREATE TABLE IF NOT EXISTS `Teaching` (
  `crsCode` varchar(40) NOT NULL,
  `semester` varchar(40) NOT NULL,
  `profId` text NOT NULL,
    PRIMARY KEY (`crsCode`, `semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Transcript`
--

CREATE TABLE IF NOT EXISTS `Transcript` (
  `studId` int(11) NOT NULL,
  `crsCode` varchar(40) NOT NULL,
  `semester` varchar(40) NOT NULL,
  `grade` text NOT NULL,
    PRIMARY KEY (`studId`, `crsCode`, `semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



