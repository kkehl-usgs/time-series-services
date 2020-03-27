package gov.usgs.wma.waterdata.collections;

import static gov.usgs.wma.waterdata.collections.CollectionParams.DEFAULT_COLLECTION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONObjectAs;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.wma.waterdata.collections.CollectionsDao;
import gov.usgs.wma.waterdata.ConfigurationService;
import gov.usgs.wma.waterdata.collections.CollectionParams;
import gov.usgs.wma.waterdata.springinit.BaseIT;
import gov.usgs.wma.waterdata.springinit.DBTestConfig;

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = {DBTestConfig.class, CollectionParams.class, CollectionsDao.class,
					ConfigurationService.class})
@DatabaseSetup("classpath:/testData/monitoringLocation/")
public class CollectionsDaoIT extends BaseIT {
	@Autowired
	private CollectionParams collectionsParams;

	@Autowired
	private CollectionsDao collectionsDao;

	@Test
	public void foundCollectionsTest() {
		try {
			String expected = getCompareFile("collections.json");
			String actual = collectionsDao.getCollectionsJson(collectionsParams.buildParams(null));
			assertJsonEquals(expected, actual);
		} catch (IOException e) {
			fail("Unexpected IOException during test", e);
		}
	}

	@Test
	public void foundMonLocCollectionTest() {
		try {
			String expected = getCompareFile("monLocsCollection.json");
			String actual = collectionsDao.getCollectionJson(collectionsParams.buildParams("monitoring-locations"));
			assertJsonEquals(expected, actual);
		} catch (IOException e) {
			fail("Unexpected IOException during test", e);
		}
	}

	@Test
	public void foundNetworkCollectionTest() {
		try {
			String expected = getCompareFile("ahsCollection.json");
			String actual = collectionsDao.getCollectionJson(collectionsParams.buildParams("AHS"));
			assertJsonEquals(expected, actual);
		} catch (IOException e) {
			fail("Unexpected IOException during test", e);
		}
	}

	@Test
	public void foundCollectionFeatureTest() {
		try {
			String expected = getCompareFile("features/monitoring-locations/USGS-07227448.json");
			String actual = collectionsDao.getCollectionFeatureJson(collectionsParams.buildParams("monitoring-locations","USGS-07227448"));
			assertJsonEquals(expected, actual);
		} catch (IOException e) {
			fail("Unexpected IOException during test", e);
		}
	}

	@Test
	public void monLocCollectionFeatureCountTest() {
		int count = collectionsDao.getCollectionFeatureCount(collectionsParams.buildParams("monitoring-locations"));
		assertTrue(count == 3);
	}

	@Test
	public void networkCollectionFeatureCountTest() {
		int count = collectionsDao.getCollectionFeatureCount(collectionsParams.buildParams("AHS"));
		assertTrue(count == 1);
	}

	@Test
	public void notFoundTest() {
		String collectionJson = collectionsDao.getCollectionJson(collectionsParams.buildParams("xyz"));
		assertNull(collectionJson);
	}

	@Test
	public void notFoundFeatureNoCollectionTest() {
		String featureJson = collectionsDao.getCollectionFeatureJson(collectionsParams.buildParams("xyz","USGS-07227448"));
		assertNull(featureJson);
	}

	@Test
	public void notFoundFeaturesNoCollectionTest() {
		String featureJson = collectionsDao.getCollectionFeaturesJson(collectionsParams.buildParams("xyz","USGS-07227448"));
		assertNull(featureJson);
	}

	@Test
	public void notFoundFeatureId() {
		String featureJson = collectionsDao.getCollectionFeatureJson(collectionsParams.buildParams("monitoring-locations","xyz"));
		assertNull(featureJson);
	}

	@Test
	public void notFoundFeatureNoGeom() {
		String featureJson = collectionsDao.getCollectionFeatureJson(collectionsParams.buildParams("monitoring-locations","USGS-04028090"));
		assertNull(featureJson);
	}

	@Test
	public void notFoundFeatureNotInCollection() {
		String featureJson = collectionsDao.getCollectionFeatureJson(collectionsParams.buildParams("AHS","USGS-07227448"));
		assertNull(featureJson);
	}


	@Test
	public void featureTimeSeriesCollectionTest() throws Exception {
		String actualJSON  = collectionsDao.getStatisticalTimeSeries(DEFAULT_COLLECTION_ID, "USGS-07227448");
		String expectJSON = getCompareFile("features/monitoring-locations/usgs-07227448-obs-list.json");
		assertThat(new JSONObject(actualJSON), sameJSONObjectAs(new JSONObject(expectJSON)));
	}

	@Test
	public void featureTimeSeriesCollectionEmptyTest() throws Exception {
		String actualJSON  = collectionsDao.getStatisticalTimeSeries(DEFAULT_COLLECTION_ID, "USGS-04027940");
		String expectJSON = getCompareFile("features/monitoring-locations/usgs-04027940-obs-list.json");
		assertThat(new JSONObject(actualJSON), sameJSONObjectAs(new JSONObject(expectJSON)));
	}

	@Test
	public void collectionMissingTest() throws Exception {
		String actualJSON  = collectionsDao.getStatisticalTimeSeries("SOME-COLLECTION", "USGS-07227448");
		assertNull(actualJSON);
	}

	@Test
	public void featureNotFoundTest() {
		String actualJSON  = collectionsDao.getStatisticalTimeSeries(DEFAULT_COLLECTION_ID, "SOME-FEATURE");
		assertNull(actualJSON);
	}

	@Test
	public void notFoundNoGeomTest() throws Exception {
		String actualJSON  = collectionsDao.getStatisticalTimeSeries(DEFAULT_COLLECTION_ID, "USGS-04028090");
		assertNull(actualJSON);
	}

}
