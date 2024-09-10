package com.vmware.data.services.gemfire.lucene.functions;

import com.vmware.data.services.gemfire.lucene.functions.domain.SearchCriteria;
import com.vmware.data.services.gemfire.lucene.functions.domain.SearchCriteriaBuilder;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.*;
import org.apache.geode.cache.lucene.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * GemFire function that using GemFire Search based on Apache Lucene.
 * This result can save to a Paging region.
 * @author gregory green
 */
public class GemFireSearchFunction implements Function<Object>
{
	private static final long serialVersionUID = 1L;
	private final Supplier<Cache> cacheSupplier;
	private final Supplier<LuceneService> luceneServiceSupplier;
	private final Logger logger = LogManager.getLogger(GemFireSearchFunction.class);

	public GemFireSearchFunction()
	{
		this( () -> CacheFactory.getAnyInstance(),
				() -> LuceneServiceProvider.get(CacheFactory.getAnyInstance()));
	}

	public GemFireSearchFunction(Supplier<Cache> cacheSupplier, Supplier<LuceneService> luceneServiceSupplier) {
		this.cacheSupplier = cacheSupplier;
		this.luceneServiceSupplier = luceneServiceSupplier;
	}


	@Override
	public String getId() {
		return "GemFireSearchFunction";
	}
	/**
	 * Execute the search on Region
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute(FunctionContext functionContext) 
	{
		Cache cache = cacheSupplier.get();
		
		try
		{
			//Function must be executed on Region
			if(functionContext instanceof RegionFunctionContext)
			{	
				throw new FunctionException("Execute on a onServer");
			}


			Object args = functionContext.getArguments();
			
			if (args == null)
				throw new FunctionException("arguments is required");
			
			SearchCriteria criteria = new SearchCriteriaBuilder().args(args).build();

			logger.info("criteria: {}",criteria);


			Region<?,?> region = cache.getRegion(criteria.getRegionName());
			if(region == null)
				throw new FunctionException("Region \""+criteria.getRegionName()+"\" does not exist");

			Region<String, Collection<Object>> pagingRegion = cache.getRegion(criteria.getPageRegionName());
			if(pagingRegion == null)
				throw new FunctionException("Paging region \""+criteria.getPageRegionName()+"\" does not exist. Please create it.");


			LuceneService luceneService = luceneServiceSupplier.get();

			LuceneQuery<Object, Object> query = luceneService.createLuceneQueryFactory()
					.setLimit(criteria.getLimit())
					.setPageSize(criteria.getPageSize())
					.create(criteria.getIndexName(),
							region.getName(),
							criteria.getQuery(),
							criteria.getDefaultField());

			ResultSender resultSender = functionContext.getResultSender();

			if (criteria.getKeysOnly()) {
				logger.info("Returning keys only");
				resultSender.lastResult(query.findKeys());
            }
			else {

				logger.info("Saving pages");
				PageableLuceneQueryResults<Object, Object> pageableLuceneQueryResults = query.findPages();

				int pageNumber = 1;

				List page = null;

				String key;


				while (pageableLuceneQueryResults.hasNext()) {
					page = pageableLuceneQueryResults.next();


					key = criteria.toPageKey(pageNumber++);
					pagingRegion.put(key,page);

					logger.info("Returning key {}",key);

					//Send key back to user
					if(!pageableLuceneQueryResults.hasNext()){
						resultSender.lastResult(key);
					}
					else
						resultSender.sendResult(key);

				}

			}
		}
		catch (LuceneQueryException luceneQueryException) {
			logger.error(luceneQueryException);
			throw new FunctionException(luceneQueryException);
		}
		catch (RuntimeException e)
		{
			logger.error(e);
			throw e;
		}
	}
}
