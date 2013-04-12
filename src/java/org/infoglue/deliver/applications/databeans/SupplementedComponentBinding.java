/**
 * 
 */
package org.infoglue.deliver.applications.databeans;

/**
 * @author Erik StenbÃ¤cka <stenbacka@gmail.com>
 */
public class SupplementedComponentBinding extends ComponentBinding
{
	private Integer supplementingEntityId;
	private String supplementingAssetKey;

	public SupplementedComponentBinding(Integer entityId, String assetKey)
	{
		this.supplementingEntityId = entityId;
		this.supplementingAssetKey = assetKey;
	}

	public Integer getSupplementingEntityId()
	{
		return supplementingEntityId;
	}

	public void setSupplementingEntityId(Integer supplementingEntityId)
	{
		this.supplementingEntityId = supplementingEntityId;
	}

	public String getSupplementingAssetKey()
	{
		return supplementingAssetKey;
	}

	public void setSupplementingAssetKey(String supplementingAssetKey)
	{
		this.supplementingAssetKey = supplementingAssetKey;
	}
}
