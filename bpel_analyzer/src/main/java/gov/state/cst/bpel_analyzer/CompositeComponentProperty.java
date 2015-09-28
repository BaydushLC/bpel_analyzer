package gov.state.cst.bpel_analyzer;

import org.apache.commons.lang.BooleanUtils;
import org.w3c.dom.Element;

public class CompositeComponentProperty {
	private String name;
	private boolean many;
	private String type;
	private String value;
	
	@SuppressWarnings("unused")
	private CompositeComponentProperty() {
	}
	
	public CompositeComponentProperty( Element propertyNode ) {
		this.name = propertyNode.getAttribute( "name" );
		this.many = BooleanUtils.toBoolean( propertyNode.getAttribute( "many" ) );
		this.type = propertyNode.getAttribute( "type" );
		this.value = propertyNode.getTextContent().trim();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	/**
	 * @return the many
	 */
	public boolean isMany() {
		return many;
	}


	/**
	 * @param many the many to set
	 */
	public void setMany(boolean many) {
		this.many = many;
	}

	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public <T> T convertTo( T defaultValue ) throws IllegalConversionException {
		if( this.type == null || this.value == null || this.value.isEmpty() ) {
			return defaultValue;
		}
		return (T)convertTo( defaultValue.getClass() );
	}
	
	public <T> T convertTo( Class<T> conversionType ) throws IllegalConversionException {
		try {
			switch( this.type.toLowerCase() ) {
			case "xs:boolean":
				if( this.value == null || this.value.isEmpty() ) {
					return conversionType.cast( false );
				}
				if( BooleanUtils.toBoolean( this.value ) || this.value.equals( "1" ) ) {
					return conversionType.cast( true );
				}
				return conversionType.cast( false );
			case "xs:int":
			case "xs:integer":
				return conversionType.cast( Integer.parseInt( this.value ) );
			default:
				throw new IllegalConversionException();
			}
		}
		catch( Exception e ) {
			if( e instanceof IllegalConversionException ) {
				throw e;
			}
			throw new IllegalConversionException( e );
		}
	}
}
