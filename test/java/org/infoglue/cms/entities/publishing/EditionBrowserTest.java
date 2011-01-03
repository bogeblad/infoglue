package org.infoglue.cms.entities.publishing;

import java.util.ArrayList;

import org.infoglue.cms.entities.ValidationTestCase;

/**
 * Test the EditionBrowser calculations
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class EditionBrowserTest extends ValidationTestCase
{
	private EditionBrowser testBrowser;

	public void testPlainCalculations() throws Exception
	{
		testBrowser = createBrowser(100, 10, 21);
		assertEquals("Wrong total pages", 10, testBrowser.getTotalPages());
		assertEquals("Wrong current page", 3, testBrowser.getCurrentPage());
		assertEquals("Wrong next page size", 10, testBrowser.getNextPageSize());
		assertEquals("Wrong previous page size", 10, testBrowser.getPreviousPageSize());
		assertTrue("Wrong hasPreviousPage", testBrowser.hasPreviousPage());
		assertTrue("Wrong hasNextPage", testBrowser.hasNextPage());
		assertEquals("Wrong previous page index", 11, testBrowser.getPreviousPageIndex());
		assertEquals("Wrong next page index", 31, testBrowser.getNextPageIndex());
	}

	public void testCurrentPage() throws Exception
	{
		testBrowser = createBrowser(14, 5, 0);
		assertEquals("Wrong current page for index 0", 1, testBrowser.getCurrentPage());

		testBrowser = createBrowser(14, 5, 1);
		assertEquals("Wrong current page for index 1", 1, testBrowser.getCurrentPage());

		testBrowser = createBrowser(14, 5, 5);
		assertEquals("Wrong current page for index 5", 2, testBrowser.getCurrentPage());

		testBrowser = createBrowser(14, 5, 6);
		assertEquals("Wrong current page for index 6", 2, testBrowser.getCurrentPage());

		testBrowser = createBrowser(14, 5, 10);
		assertEquals("Wrong current page for index 10", 3, testBrowser.getCurrentPage());

		testBrowser = createBrowser(14, 5, 11);
		assertEquals("Wrong current page for index 11", 3, testBrowser.getCurrentPage());
	}

	public void testTotalPages() throws Exception
	{
		testBrowser = createBrowser(0, 5, 0);
		assertEquals("Wrong total pages for size zero", 1, testBrowser.getTotalPages());

		testBrowser = createBrowser(1, 5, 0);
		assertEquals("Wrong total pages for size one", 1, testBrowser.getTotalPages());

		testBrowser = createBrowser(5, 5, 0);
		assertEquals("Wrong total pages for size five", 1, testBrowser.getTotalPages());

		testBrowser = createBrowser(6, 5, 0);
		assertEquals("Wrong total pages for size six", 2, testBrowser.getTotalPages());

		testBrowser = createBrowser(101, 5, 0);
		assertEquals("Wrong total pages for size 101", 21, testBrowser.getTotalPages());
	}

	public void testPrevious() throws Exception
	{
		testBrowser = createBrowser(10, 5, 0);
		assertEquals("Wrong previous page size for index 0", 0, testBrowser.getPreviousPageSize());
		assertEquals("Wrong previous page index for index 0 ", 0, testBrowser.getPreviousPageIndex());
		assertFalse("Has previous page for index 0", testBrowser.hasPreviousPage());

		testBrowser = createBrowser(10, 5, 1);
		assertEquals("Wrong previous page size for index 1", 1, testBrowser.getPreviousPageSize());
		assertEquals("Wrong previous page index for index 1 ", 0, testBrowser.getPreviousPageIndex());
		assertTrue("Does not have previous page for index 1", testBrowser.hasPreviousPage());

		testBrowser = createBrowser(10, 5, 5);
		assertEquals("Wrong previous page size for index 5", 5, testBrowser.getPreviousPageSize());
		assertEquals("Wrong previous page index for index 5 ", 0, testBrowser.getPreviousPageIndex());
		assertTrue("Does not have previous page for index 5", testBrowser.hasPreviousPage());

		testBrowser = createBrowser(10, 5, 6);
		assertEquals("Wrong previous page size for index 6", 5, testBrowser.getPreviousPageSize());
		assertEquals("Wrong previous page index for index 6 ", 1, testBrowser.getPreviousPageIndex());
		assertTrue("Does not have previous page for index 6", testBrowser.hasPreviousPage());
	}

	public void testNext() throws Exception
	{
		testBrowser = createBrowser(10, 5, 0);
		assertEquals("Wrong next page size for index 0", 5, testBrowser.getNextPageSize());
		assertEquals("Wrong next page index for index 0 ", 5, testBrowser.getNextPageIndex());
		assertTrue("Does not have next page for index 0", testBrowser.hasNextPage());

		testBrowser = createBrowser(10, 5, 1);
		assertEquals("Wrong next page size for index 1", 4, testBrowser.getNextPageSize());
		assertEquals("Wrong next page index for index 1 ", 6, testBrowser.getNextPageIndex());
		assertTrue("Does not have next page for index 1", testBrowser.hasNextPage());

		testBrowser = createBrowser(10, 5, 5);
		assertEquals("Wrong next page size for index 5", 0, testBrowser.getNextPageSize());
		assertEquals("Wrong next page index for index 5 ", 10, testBrowser.getNextPageIndex());
		assertFalse("Has have next page for index 5", testBrowser.hasNextPage());

		testBrowser = createBrowser(10, 5, 6);
		assertEquals("Wrong next page size for index 6", 0, testBrowser.getNextPageSize());
		assertEquals("Wrong next page index for index 6 ", 10, testBrowser.getNextPageIndex());
		assertFalse("Has next page for index 6", testBrowser.hasNextPage());
	}

	private EditionBrowser createBrowser(int totalEditions, int pageSize, int startIndex)
	{
		EditionBrowser browser = new EditionBrowser(totalEditions, pageSize, startIndex);

		int numOfEditions = Math.min(pageSize, totalEditions - startIndex);
		ArrayList editions = new ArrayList();
		for (int i = 0; i < numOfEditions; i++)
			editions.add(new PublicationVO());

		browser.setEditions(editions);
		return browser;
	}
}
