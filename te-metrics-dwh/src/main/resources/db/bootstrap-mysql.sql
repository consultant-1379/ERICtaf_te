
-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema taf_performance
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `taf_performance` DEFAULT CHARACTER SET latin1 ;
USE `taf_performance` ;

-- -----------------------------------------------------
-- Table `taf_performance`.`executions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taf_performance`.`executions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `taf_performance`.`time`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taf_performance`.`time` (
  `id` TIMESTAMP NOT NULL,
  `day` DATE NOT NULL,
  `hour` INT(11) NOT NULL,
  `minute` INT(11) NOT NULL,
  PRIMARY KEY (`id`))
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `taf_performance`.`test_suites`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taf_performance`.`test_suites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `execution_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_test_suites_executions1_idx` (`execution_id` ASC),
  UNIQUE INDEX `name_execution_id_UNIQUE` (`name` ASC, `execution_id` ASC),
  CONSTRAINT `fk_test_suites_executions1`
  FOREIGN KEY (`execution_id`)
  REFERENCES `taf_performance`.`executions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `taf_performance`.`test_cases`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taf_performance`.`test_cases` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `test_suite_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_test_cases_test_suites1_idx` (`test_suite_id` ASC),
  UNIQUE INDEX `name_test_suite_id_UNIQUE` (`name` ASC, `test_suite_id` ASC),
  CONSTRAINT `fk_test_cases_test_suites1`
  FOREIGN KEY (`test_suite_id`)
  REFERENCES `taf_performance`.`test_suites` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `taf_performance`.`samples`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taf_performance`.`samples` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `thread_id` BIGINT(20) NOT NULL,
  `vuser_id` VARCHAR(255) NOT NULL,
  `protocol` VARCHAR(255) NOT NULL,
  `target` TEXT NOT NULL,
  `request_type` VARCHAR(255) NOT NULL,
  `request_size` BIGINT(20) NOT NULL,
  `response_code` INT(11) NOT NULL,
  `success` SMALLINT(2) NOT NULL,
  `response_time` INT(11) NOT NULL,
  `latency` INT(11) NOT NULL,
  `response_size` BIGINT(20) NOT NULL,
  `time_id` TIMESTAMP NOT NULL,
  `test_case_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_samples_time_idx` (`time_id` ASC),
  INDEX `fk_samples_test_cases1_idx` (`test_case_id` ASC),
  CONSTRAINT `fk_samples_time`
  FOREIGN KEY (`time_id`)
  REFERENCES `taf_performance`.`time` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_samples_test_cases1`
  FOREIGN KEY (`test_case_id`)
  REFERENCES `taf_performance`.`test_cases` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `taf_performance`.`sample_data`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `taf_performance`.`sample_data` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `request_body` TEXT NULL,
  `request_headers` TEXT NULL,
  `response_body` TEXT NULL,
  `response_headers` TEXT NULL,
  `sample_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_sample_data_samples1_idx` (`sample_id` ASC),
  CONSTRAINT `fk_sample_data_samples1`
  FOREIGN KEY (`sample_id`)
  REFERENCES `taf_performance`.`samples` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB
  DEFAULT CHARACTER SET = latin1;

