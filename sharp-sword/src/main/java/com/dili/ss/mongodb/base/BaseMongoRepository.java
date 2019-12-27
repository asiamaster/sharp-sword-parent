package com.dili.ss.mongodb.base;

import com.dili.ss.util.ReflectionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

/**
 *  Mongo扩展基类<BR/>
 *  简单处理请实现MongoRepository<T, ID extends Serializable>接口
 * Created by asiamastor on 2017/1/3.
 */
public class BaseMongoRepository<E>  {

    /** 日志对象 */
    protected static final Logger logger = LoggerFactory
            .getLogger(BaseMongoRepository.class);
    @Autowired
    protected MongoTemplate mongoTemplate;

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Entity的类型
     */
    protected Class<E> entityClass;

    public BaseMongoRepository() {
        this.entityClass = ReflectionUtils.getSuperClassGenricType(getClass());
    }

    /**
     *
     * @Title: findAll
     * @Description: 查询表格所有的数据
     * @return List<E> 当前实体对应的表格的所有数据。
     */
    public List<E> findAll() {
        return this.findAll(null);
    }

    /**
     * @Title: findAll
     * @Description: 查询表格所有的数据
     *            该表所对应的实体
     * @param collcetionName
     *            表格名称
     * @return List<E>
     */
    public List<E> findAll(String collcetionName) {

        if (StringUtils.isBlank(collcetionName)) {
            collcetionName = mongoTemplate.getCollectionName(ReflectionUtils.getSuperClassGenricType(getClass()));
            if (StringUtils.isBlank(collcetionName)) {
                collcetionName = this.entityClass.getSimpleName().toLowerCase();
            }
            logger.info("findAll's param collcetionName is null,so it default is "
                    + collcetionName);
        }
        return mongoTemplate.findAll(entityClass, collcetionName);
    }

    /**
     *
     * @Title: findCount
     * @Description:查询当前实体对应表格的所有数据的条数
     * @return long 总条数
     * @throws
     */
    public long findCount() {
        return mongoTemplate.count(new Query(), entityClass);
    }

    /***
     *
     * @Title: insert
     * @Description: 入库
     * @param e
     *            实体数据
     * @param collectionName
     *            表名
     */
    public void insert(E e, String collectionName) {
        mongoTemplate.insert(e, collectionName);
    }

    /**
     *
     * @Title: insert
     * @Description: 入库
     * @param e
     *            实体数据
     */
    public void insert(E e) {
        mongoTemplate.insert(e);
    }

    /**
     * 生产查询语句
     *
     * @Title: createCriteria
     * @Description: 根据不同条件生产SQL
     * @param gtMap
     * @param ltMap
     * @param eqMap
     * @param gteMap
     * @param lteMap
     * @param regexMap
     * @param inMap
     * @param neMap
     * @return Criteria 查询的语句
     * @throws
     */
    @SuppressWarnings("rawtypes")
    public Criteria createCriteria(Map<String, Object> gtMap,
                                   Map<String, Object> ltMap, Map<String, Object> eqMap,
                                   Map<String, Object> gteMap, Map<String, Object> lteMap,
                                   Map<String, String> regexMap, Map<String, Collection> inMap,
                                   Map<String, Object> neMap) {
        Criteria c = new Criteria();
        List<Criteria> listC= new ArrayList<Criteria>();
        Set<String> _set = null;
        if (gtMap != null && gtMap.size() > 0) {
            _set = gtMap.keySet();
            for (String _s : _set) {
                listC.add( Criteria.where(_s).gt(gtMap.get(_s)));
            }
        }
        if (ltMap != null && ltMap.size() > 0) {
            _set = ltMap.keySet();
            for (String _s : _set) {
                listC.add(  Criteria.where(_s).lt(ltMap.get(_s)));
            }
        }
        if (eqMap != null && eqMap.size() > 0) {
            _set = eqMap.keySet();
            for (String _s : _set) {
                listC.add(  Criteria.where(_s).is(eqMap.get(_s)));
            }
        }
        if (gteMap != null && gteMap.size() > 0) {
            _set = gteMap.keySet();
            for (String _s : _set) {
                listC.add( Criteria.where(_s).gte(gteMap.get(_s)));
            }
        }
        if (lteMap != null && lteMap.size() > 0) {
            _set = lteMap.keySet();
            for (String _s : _set) {
                listC.add(  Criteria.where(_s).lte(lteMap.get(_s)));
            }
        }

        if (regexMap != null && regexMap.size() > 0) {
            _set = regexMap.keySet();
            for (String _s : _set) {
                listC.add(  Criteria.where(_s).regex(regexMap.get(_s)));
            }
        }

        if (inMap != null && inMap.size() > 0) {
            _set = inMap.keySet();
            for (String _s : _set) {
                listC.add(  Criteria.where(_s).in(inMap.get(_s)));
            }
        }
        if (neMap != null && neMap.size() > 0) {
            _set = neMap.keySet();
            for (String _s : _set) {
                listC.add( Criteria.where(_s).ne(neMap.get(_s)));
            }
        }
        if(listC.size() > 0){
            Criteria [] cs = new Criteria[listC.size()];
            c.andOperator(listC.toArray(cs));
        }
        return c;
    }

    public Criteria createCriteria(Map<String, Object> eqMap) {
        return this.createCriteria(null, null, eqMap, null, null, null, null,
                null);
    }

    public Criteria createCriteria(Map<String, Object> eqMap,
                                   Map<String, Object> neMap) {
        return this.createCriteria(null, null, eqMap, null, null, null, null,
                neMap);
    }

    /**
     *
     * @Title: findCount
     * @Description: 根据各种条件查询总数
     * @param gtMap
     * @param ltMap
     * @param eqMap
     * @param gteMap
     * @param lteMap
     * @param regexMap
     * @param inMap
     * @param neMap
     * @return long 总数
     * @throws
     */
    @SuppressWarnings("rawtypes")
    public long findCount(Map<String, Object> gtMap, Map<String, Object> ltMap,
                          Map<String, Object> eqMap, Map<String, Object> gteMap,
                          Map<String, Object> lteMap, Map<String, String> regexMap,
                          Map<String, Collection> inMap, Map<String, Object> neMap) {
        long count = 0;
        Criteria c = this.createCriteria(gtMap, ltMap, eqMap, gteMap, lteMap,
                regexMap, inMap, neMap);
        Query query = new Query(c);
        count = mongoTemplate.count(query, entityClass);
        return count;
    }
    /***
     *
     * @Title: findCount
     * @Description: 根据创建的条件查询总数
     * @param  queryC
     * @return long    返回类型
     * @throws
     */
    public long findCount(Criteria queryC){
        Query query = new Query(queryC);
        return mongoTemplate.count(query, entityClass);
    }

    /**
     *
     * @Title: findCount
     * @Description: 根据多个种条件 or 的方式查询
     * @param orList
     *            or的查询条件的集合
     * @return long
     * @throws
     */
    public long findCount(Criteria... orList) {
        long count = 0;
        Criteria c = new Criteria();
        Query query = null;
        if (orList != null && orList.length > 0) {
            c.orOperator(orList);
        }
        query = new Query(c);

        count = mongoTemplate.count(query, entityClass);
        return count;
    }

    @SuppressWarnings("rawtypes")
    public long findCount(Map<String, Object> gtMap, Map<String, Object> ltMap,
                          Map<String, Object> eqMap, Map<String, String> regexMap,
                          Map<String, Collection> inMap) {
        return this.findCount(gtMap, ltMap, eqMap, null, null, regexMap, inMap,
                null);
    }

    public long findCountByContainRegex(Map<String, Object> gtMap,
                                        Map<String, Object> ltMap, Map<String, Object> eqMap,
                                        Map<String, String> regexMap) {
        return this.findCount(gtMap, ltMap, eqMap, regexMap, null);
    }


    /**
     *
     * @Title: findListByPage
     * @Description: 根据分页+条件获取对应的实体集合
     * @param eqMap
     * @param gtMap
     * @param ltMap
     * @param gteMap
     * @param lteMap
     * @param regexMap
     * @param inMap
     * @param orders
     *            排序集合
     * @param pageIndex
     *            页码(从1开始)
     * @param pageSize
     *            每页条数
     * @return List<E> 实体集合
     * @throws
     */
    @SuppressWarnings("rawtypes")
    public List<E> findListByPage(Map<String, Object> eqMap,
                                  Map<String, Object> gtMap, Map<String, Object> ltMap,
                                  Map<String, Object> gteMap, Map<String, Object> lteMap,
                                  Map<String, String> regexMap, Map<String, Collection> inMap,
                                  Map<String, Object> neMap, List<Order> orders, int pageIndex,
                                  int pageSize) {
        List<E> list = null;
        Criteria c = this.createCriteria(gtMap, ltMap, eqMap, gteMap, lteMap,
                regexMap, inMap, neMap);
        Query query = null;
        if (c == null) {
            query = new Query();
        } else {
            query = new Query(c);
        }
        if (CollectionUtils.isNotEmpty(orders)) {
            query = query.with(Sort.by(orders));
        }
        if (pageSize > 0) {
            query.skip((pageIndex - 1) * pageSize);
            query.limit(pageSize);
        }
        list = mongoTemplate.find(query, entityClass);

        return list;
    }
    /**
     * @Title: findOneObject
     * @Description: 符合条件的某一条数据
     * @param eqMap
     * @param gtMap
     * @param ltMap
     * @param gteMap
     * @param lteMap
     * @param regexMap
     * @param inMap
     * @return E 返回该数据对应的实体
     */
    @SuppressWarnings("rawtypes")
    public E findObject(Map<String, Object> eqMap, Map<String, Object> gtMap,
                        Map<String, Object> ltMap, Map<String, Object> gteMap,
                        Map<String, Object> lteMap, Map<String, String> regexMap,
                        Map<String, Collection> inMap) {
        E e = null;
        List<E> list = this.findList(createCriteria(eqMap, gtMap, ltMap, gteMap, lteMap,
                regexMap, inMap, null));
        if (list != null && list.size() > 0) {
            e = list.get(0);
        }
        return e;
    }

    /**
     *
     * @Title: findList
     * @Description: 多个查询条件or方式组合查询
     * @param orList
     * @return List<E>
     * @throws
     */
    public List<E> findList(Criteria... orList) {
        return this.findListByPage(null, 0, 0, orList);
    }

    /**
     *
     * @Title: findListByOrder
     * @Description: 多个查询条件or方式组合查询
     * @param orList
     * @param orders
     * @return List<E>
     * @throws
     */
    public List<E> findListByOrder(List<Order> orders, Criteria... orList) {
        return this.findListByPage(orders, 0, 0, orList);
    }
    /**
     *
     * @Title: findListByPage
     * @Description: 根据查询条件直接查询
     * @param  c
     * @param  orders
     * @param  pageIndex (从1开始)
     * @param  pageSize
     * @return List<E>
     * @throws
     */
    public List<E> findListByPage(Criteria c, List<Order> orders, int pageIndex,
                                  int pageSize){
        Query query = new Query(c);
        if (CollectionUtils.isNotEmpty(orders)) {
            query = query.with(Sort.by(orders));
        }
        if (pageSize > 0) {
            query.skip((pageIndex - 1) * pageSize);
            query.limit(pageSize);
        }
        return mongoTemplate.find(query, entityClass);
    }
    public List<E> findListByOrder(Criteria c, List<Order> orders){
        return this.findListByPage(c, orders, 0, 0);
    }
    public List<E> findList(Criteria c){
        return this.findListByPage(c, null, 0, 0);
    }
    /**
     *
     * @Title: findObject
     * @Description: 根据查询条件查询某一个object
     * @param  c
     * @return E
     * @throws
     */
    public E findObject(Criteria c){
        List<E> list = this.findList(c);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }
    /**
     *
     * @Title: findListByPage
     * @param orders 排序规则
     * @param pageIndex
     *            第几页(从1开始)
     * @param pageSize 每页多少条
     * @return List<E> 符合条件的集合
     * @throws
     */
    public List<E> findListByPage(List<Order> orders, int pageIndex,
                                  int pageSize, Criteria... orList) {
        List<E> list = null;
        Criteria c = new Criteria();
        Query query = null;
        if (orList != null && orList.length > 0) {
            c.orOperator(orList);
        }
        query = new Query(c);
        if (CollectionUtils.isNotEmpty(orders)) {
            query = query.with(Sort.by(orders));
        }
        if (pageSize > 0) {
            query.skip((pageIndex - 1) * pageSize);
            query.limit(pageSize);
        }
        list = mongoTemplate.find(query, entityClass);
        return list;
    }

    @SuppressWarnings("rawtypes")
    public List<E> findListNotContainOrder(Map<String, Object> eqMap,
                                           Map<String, Object> gtMap, Map<String, Object> ltMap,
                                           Map<String, Object> gteMap, Map<String, Object> lteMap,
                                           Map<String, String> regexMap, Map<String, Collection> inMap,
                                           Map<String, Object> neMap) {
        return this.findList(createCriteria(eqMap, gtMap, ltMap, gteMap, lteMap, regexMap,
                inMap, neMap));
    }

    protected Query pagableToQuery(Query query, Pageable pageable){
        if(pageable.getSort() != null) {
            query = query.with(pageable.getSort());
        }
        int pageSize = pageable.getPageSize();
        int pageIndex = pageable.getPageNumber();
        if(pageSize > 0) {
            query.skip((pageIndex - 1) * pageSize);
            query.limit(pageSize);
        }
        return query;
    }
}
