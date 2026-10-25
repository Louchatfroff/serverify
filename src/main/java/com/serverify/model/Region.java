// SPDX-License-Identifier: GPL-3.0-only
// Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

package com.serverify.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum Region {
	NORTH_AMERICA("US CA MX GT BZ SV HN NI CR PA CU JM HT DO BS BB TT PR GL"),
	SOUTH_AMERICA("BR AR CL CO PE VE EC BO PY UY GY SR"),
	EUROPE("GB IE FR DE ES PT IT NL BE LU CH AT DK SE NO FI IS PL CZ SK HU RO BG GR HR SI RS BA ME MK AL XK MD UA BY LT LV EE RU CY MT TR AD MC LI SM"),
	ASIA("CN JP KR KP IN PK BD LK NP ID MY SG TH VN PH TW HK MO MN KZ UZ KG TJ TM IR IQ IL SA AE QA KW BH OM JO LB SY YE AF AM AZ GE KH LA MM BN BT MV"),
	OCEANIA("AU NZ FJ PG NC PF WS TO VU SB"),
	AFRICA("ZA EG NG KE MA DZ TN GH ET TZ UG SN CI CM AO MZ ZW ZM BW NA MU LY SD");

	private static final Map<String, Region> BY_COUNTRY = new HashMap<>();
	private static final Map<String, String> CODES = new HashMap<>();

	private static final String NAMES = "United States of America=US;United States=US;Canada=CA;Mexico=MX;Brazil=BR;Argentina=AR;Chile=CL;Colombia=CO;"
			+ "Peru=PE;Venezuela=VE;United Kingdom=GB;Ireland=IE;France=FR;Germany=DE;Spain=ES;Portugal=PT;Italy=IT;Netherlands=NL;Belgium=BE;"
			+ "Switzerland=CH;Austria=AT;Denmark=DK;Sweden=SE;Norway=NO;Finland=FI;Iceland=IS;Poland=PL;Czech Republic=CZ;Czechia=CZ;Slovakia=SK;"
			+ "Hungary=HU;Romania=RO;Bulgaria=BG;Greece=GR;Croatia=HR;Serbia=RS;Ukraine=UA;Lithuania=LT;Latvia=LV;Estonia=EE;Russia=RU;"
			+ "Russian Federation=RU;Turkey=TR;Turkiye=TR;China=CN;Japan=JP;South Korea=KR;Korea=KR;India=IN;Pakistan=PK;Bangladesh=BD;"
			+ "Indonesia=ID;Malaysia=MY;Singapore=SG;Thailand=TH;Vietnam=VN;Philippines=PH;Taiwan=TW;Hong Kong=HK;Kazakhstan=KZ;Iran=IR;"
			+ "Israel=IL;Saudi Arabia=SA;United Arab Emirates=AE;Australia=AU;New Zealand=NZ;South Africa=ZA;Egypt=EG;Nigeria=NG;Morocco=MA;"
			+ "Algeria=DZ;Tunisia=TN;Kenya=KE";

	static {
		for (Region region : values()) {
			for (String code : region.countries.split(" ")) {
				BY_COUNTRY.put(code, region);
			}
		}
		for (String pair : NAMES.split(";")) {
			String[] parts = pair.split("=");
			CODES.put(parts[0].toLowerCase(Locale.ROOT), parts[1]);
		}
	}

	private final String countries;

	Region(String countries) {
		this.countries = countries;
	}

	public String translationKey() {
		return "serverify.region." + name().toLowerCase(Locale.ROOT);
	}

	public boolean matches(String country) {
		Region region = of(country);
		return region == null || region == this;
	}

	public static String codeOf(String countryName) {
		String code = CODES.get(countryName.trim().replace('_', ' ').toLowerCase(Locale.ROOT));
		return code == null ? "" : code;
	}

	public static Region of(String country) {
		return country == null ? null : BY_COUNTRY.get(country.trim().toUpperCase(Locale.ROOT));
	}

	public static Region local() {
		return of(Locale.getDefault().getCountry());
	}
}
