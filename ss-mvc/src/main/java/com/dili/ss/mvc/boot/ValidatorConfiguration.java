//package com.dili.ss.mvc.boot;
//
//import org.hibernate.validator.HibernateValidator;
//
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
//
///**
// * Created by asiam on 2018/3/9 0009.
// */
////@Configuration
//public class ValidatorConfiguration {
////    @Bean
//    public Validator validator(){
//        ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
//                .configure()
//                .addProperty( "hibernate.validator.fail_fast", "true" )
//                .buildValidatorFactory();
//        Validator validator = validatorFactory.getValidator();
//
//        return validator;
//    }
//}