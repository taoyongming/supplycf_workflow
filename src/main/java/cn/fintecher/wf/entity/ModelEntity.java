package cn.fintecher.wf.entity;

import org.hibernate.validator.constraints.NotBlank;
import cn.fintecher.wf.utils.validator.group.AddGroup;
import cn.fintecher.wf.utils.validator.group.UpdateGroup;
import java.io.Serializable;

/**
 * 模型
 */
public class ModelEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * key
	 */
	@NotBlank(message="Key不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String key;

	/**
	 * 名称
	 */
	@NotBlank(message="名称不能为空", groups = AddGroup.class)
	private String name;

	/**
	 * 描述
	 */
	private String description;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "ModelEntity [key=" + key + ", name=" + name + ", description=" + description + "]";
	}
	
}
