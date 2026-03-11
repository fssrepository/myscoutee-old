import { Injectable } from '@angular/core';

export interface Option {
  value: string;
  viewValue: string;
}

const professions: Option[] = [
  { value: 'm', viewValue: 'Management' },
  { value: 'bfo', viewValue: 'Business and Financial Operations' },
  { value: 'cm', viewValue: 'Computer and Mathematical' },
  { value: 'ae', viewValue: 'Architecture and Engineering' },
  { value: 'lps', viewValue: 'Life, Physical and Social Science' },
  { value: 'css', viewValue: 'Community and Social Service' },
  { value: 'l', viewValue: 'Legal' },
  { value: 'eil', viewValue: 'Educational Instruction and Library' },
  {
    value: 'adesm',
    viewValue: 'Arts, Design, Entertainment, Sports, and Media',
  },
  { value: 'hpt', viewValue: 'Healthcare Practitioners and Technical' },
  { value: 'hs', viewValue: 'Healthcare Support' },
  { value: 'ps', viewValue: 'Protective Service' },
  { value: 'fps', viewValue: 'Food Preparation and Serving' },
  {
    value: 'bgcm',
    viewValue: 'Building and Grounds Cleaning and Maintenance',
  },
  { value: 'pcs', viewValue: 'Personal Care and Service' },
  { value: 's', viewValue: 'Sales' },
  { value: 'oas', viewValue: 'Office and Administrative Support' },
  { value: 'fff', viewValue: 'Farming, Fishing, and Forestry' },
  { value: 'ce', viewValue: 'Construction and Extraction' },
  { value: 'imr', viewValue: 'Installation, Maintenance, and Repair' },
  { value: 'p', viewValue: 'Production' },
  { value: 'tmm', viewValue: 'Transportation and Material Moving' },
  { value: 'ms', viewValue: 'Military Specific' },
];

const physiques: Option[] = [
  { value: 's', viewValue: 'Slim' },
  { value: 'a', viewValue: 'Average' },
  { value: 'sp', viewValue: 'Sum plus' },
  { value: 'm', viewValue: 'Muscular' },
];

const religions: Option[] = [
  { value: 'ch', viewValue: 'Christianity' },
  { value: 'is', viewValue: 'Islam' },
  { value: 'hi', viewValue: 'Hinduism' },
  { value: 'ci', viewValue: 'Chinese' },
  { value: 'bu', viewValue: 'Buddhism' },
  { value: 'pr', viewValue: 'Primal-indigenous' },
  { value: 'af', viewValue: 'African' },
  { value: 'si', viewValue: 'Sikhism' },
  { value: 'juc', viewValue: 'Juche' },
  { value: 'sp', viewValue: 'Spiritism' },
  { value: 'jud', viewValue: 'Judaism' },
  { value: 'ba', viewValue: 'Bahai' },
  { value: 'ja', viewValue: 'Jainism' },
  { value: 'sh', viewValue: 'Shinto' },
  { value: 'ca', viewValue: 'Cao Dai' },
  { value: 'zo', viewValue: 'Zoroastrianism' },
  { value: 'te', viewValue: 'Tenrikyo' },
  { value: 'neo', viewValue: 'Neo-Paganism' },
  { value: 'un', viewValue: 'Unitarian-Universalism' },
  { value: 'nr', viewValue: 'Nonreligious' },
];

const languages: Option[] = [
  { viewValue: 'Afar', value: 'aa' },
  { viewValue: 'Abkhazian', value: 'ab' },
  { viewValue: 'Avestan', value: 'ae' },
  { viewValue: 'Afrikaans', value: 'af' },
  { viewValue: 'Akan', value: 'ak' },
  { viewValue: 'Amharic', value: 'am' },
  { viewValue: 'Aragonese', value: 'an' },
  { viewValue: 'Arabic', value: 'ar' },
  { viewValue: 'Assamese', value: 'as' },
  { viewValue: 'Avaric', value: 'av' },
  { viewValue: 'Aymara', value: 'ay' },
  { viewValue: 'Azerbaijani', value: 'az' },
  { viewValue: 'Bashkir', value: 'ba' },
  { viewValue: 'Belarusian', value: 'be' },
  { viewValue: 'Bulgarian', value: 'bg' },
  { viewValue: 'Bihari languages', value: 'bh' },
  { viewValue: 'Bislama', value: 'bi' },
  { viewValue: 'Bambara', value: 'bm' },
  { viewValue: 'Bengali', value: 'bn' },
  { viewValue: 'Tibetan', value: 'bo' },
  { viewValue: 'Breton', value: 'br' },
  { viewValue: 'Bosnian', value: 'bs' },
  { viewValue: 'Catalan; Valencian', value: 'ca' },
  { viewValue: 'Chechen', value: 'ce' },
  { viewValue: 'Chamorro', value: 'ch' },
  { viewValue: 'Corsican', value: 'co' },
  { viewValue: 'Cree', value: 'cr' },
  { viewValue: 'Czech', value: 'cs' },
  {
    viewValue:
      'Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic',
    value: 'cu',
  },
  { viewValue: 'Chuvash', value: 'cv' },
  { viewValue: 'Welsh', value: 'cy' },
  { viewValue: 'Danish', value: 'da' },
  { viewValue: 'German', value: 'de' },
  { viewValue: 'Divehi; Dhivehi; Maldivian', value: 'dv' },
  { viewValue: 'Dzongkha', value: 'dz' },
  { viewValue: 'Ewe', value: 'ee' },
  { viewValue: 'Greek, Modern (1453-)', value: 'el' },
  { viewValue: 'English', value: 'en' },
  { viewValue: 'Esperanto', value: 'eo' },
  { viewValue: 'Spanish; Castilian', value: 'es' },
  { viewValue: 'Estonian', value: 'et' },
  { viewValue: 'Basque', value: 'eu' },
  { viewValue: 'Persian', value: 'fa' },
  { viewValue: 'Fulah', value: 'ff' },
  { viewValue: 'Finnish', value: 'fi' },
  { viewValue: 'Fijian', value: 'fj' },
  { viewValue: 'Faroese', value: 'fo' },
  { viewValue: 'French', value: 'fr' },
  { viewValue: 'Western Frisian', value: 'fy' },
  { viewValue: 'Irish', value: 'ga' },
  { viewValue: 'Gaelic; Scottish Gaelic', value: 'gd' },
  { viewValue: 'Galician', value: 'gl' },
  { viewValue: 'Guarani', value: 'gn' },
  { viewValue: 'Gujarati', value: 'gu' },
  { viewValue: 'Manx', value: 'gv' },
  { viewValue: 'Hausa', value: 'ha' },
  { viewValue: 'Hebrew', value: 'he' },
  { viewValue: 'Hindi', value: 'hi' },
  { viewValue: 'Hiri Motu', value: 'ho' },
  { viewValue: 'Croatian', value: 'hr' },
  { viewValue: 'Haitian; Haitian Creole', value: 'ht' },
  { viewValue: 'Hungarian', value: 'hu' },
  { viewValue: 'Armenian', value: 'hy' },
  { viewValue: 'Herero', value: 'hz' },
  {
    viewValue: 'Interlingua (International Auxiliary Language Association)',
    value: 'ia',
  },
  { viewValue: 'Indonesian', value: 'id' },
  { viewValue: 'Interlingue; Occidental', value: 'ie' },
  { viewValue: 'Igbo', value: 'ig' },
  { viewValue: 'Sichuan Yi; Nuosu', value: 'ii' },
  { viewValue: 'Inupiaq', value: 'ik' },
  { viewValue: 'Ido', value: 'io' },
  { viewValue: 'Icelandic', value: 'is' },
  { viewValue: 'Italian', value: 'it' },
  { viewValue: 'Inuktitut', value: 'iu' },
  { viewValue: 'Japanese', value: 'ja' },
  { viewValue: 'Javanese', value: 'jv' },
  { viewValue: 'Georgian', value: 'ka' },
  { viewValue: 'Kongo', value: 'kg' },
  { viewValue: 'Kikuyu; Gikuyu', value: 'ki' },
  { viewValue: 'Kuanyama; Kwanyama', value: 'kj' },
  { viewValue: 'Kazakh', value: 'kk' },
  { viewValue: 'Kalaallisut; Greenlandic', value: 'kl' },
  { viewValue: 'Central Khmer', value: 'km' },
  { viewValue: 'Kannada', value: 'kn' },
  { viewValue: 'Korean', value: 'ko' },
  { viewValue: 'Kanuri', value: 'kr' },
  { viewValue: 'Kashmiri', value: 'ks' },
  { viewValue: 'Kurdish', value: 'ku' },
  { viewValue: 'Komi', value: 'kv' },
  { viewValue: 'Cornish', value: 'kw' },
  { viewValue: 'Kirghiz; Kyrgyz', value: 'ky' },
  { viewValue: 'Latin', value: 'la' },
  { viewValue: 'Luxembourgish; Letzeburgesch', value: 'lb' },
  { viewValue: 'Ganda', value: 'lg' },
  { viewValue: 'Limburgan; Limburger; Limburgish', value: 'li' },
  { viewValue: 'Lingala', value: 'ln' },
  { viewValue: 'Lao', value: 'lo' },
  { viewValue: 'Lithuanian', value: 'lt' },
  { viewValue: 'Luba-Katanga', value: 'lu' },
  { viewValue: 'Latvian', value: 'lv' },
  { viewValue: 'Malagasy', value: 'mg' },
  { viewValue: 'Marshallese', value: 'mh' },
  { viewValue: 'Maori', value: 'mi' },
  { viewValue: 'Macedonian', value: 'mk' },
  { viewValue: 'Malayalam', value: 'ml' },
  { viewValue: 'Mongolian', value: 'mn' },
  { viewValue: 'Marathi', value: 'mr' },
  { viewValue: 'Malay', value: 'ms' },
  { viewValue: 'Maltese', value: 'mt' },
  { viewValue: 'Burmese', value: 'my' },
  { viewValue: 'Nauru', value: 'na' },
  {
    viewValue: 'Bokm\u00e5l, Norwegian; Norwegian Bokm\u00e5l',
    value: 'nb',
  },
  { viewValue: 'Ndebele, North; North Ndebele', value: 'nd' },
  { viewValue: 'Nepali', value: 'ne' },
  { viewValue: 'Ndonga', value: 'ng' },
  { viewValue: 'Dutch; Flemish', value: 'nl' },
  { viewValue: 'Norwegian Nynorsk; Nynorsk, Norwegian', value: 'nn' },
  { viewValue: 'Norwegian', value: 'no' },
  { viewValue: 'Ndebele, South; South Ndebele', value: 'nr' },
  { viewValue: 'Navajo; Navaho', value: 'nv' },
  { viewValue: 'Chichewa; Chewa; Nyanja', value: 'ny' },
  { viewValue: 'Occitan (post 1500)', value: 'oc' },
  { viewValue: 'Ojibwa', value: 'oj' },
  { viewValue: 'Oromo', value: 'om' },
  { viewValue: 'Oriya', value: 'or' },
  { viewValue: 'Ossetian; Ossetic', value: 'os' },
  { viewValue: 'Panjabi; Punjabi', value: 'pa' },
  { viewValue: 'Pali', value: 'pi' },
  { viewValue: 'Polish', value: 'pl' },
  { viewValue: 'Pushto; Pashto', value: 'ps' },
  { viewValue: 'Portuguese', value: 'pt' },
  { viewValue: 'Quechua', value: 'qu' },
  { viewValue: 'Romansh', value: 'rm' },
  { viewValue: 'Rundi', value: 'rn' },
  { viewValue: 'Romanian; Moldavian; Moldovan', value: 'ro' },
  { viewValue: 'Russian', value: 'ru' },
  { viewValue: 'Kinyarwanda', value: 'rw' },
  { viewValue: 'Sanskrit', value: 'sa' },
  { viewValue: 'Sardinian', value: 'sc' },
  { viewValue: 'Sindhi', value: 'sd' },
  { viewValue: 'Northern Sami', value: 'se' },
  { viewValue: 'Sango', value: 'sg' },
  { viewValue: 'Sinhala; Sinhalese', value: 'si' },
  { viewValue: 'Slovak', value: 'sk' },
  { viewValue: 'Slovenian', value: 'sl' },
  { viewValue: 'Samoan', value: 'sm' },
  { viewValue: 'Shona', value: 'sn' },
  { viewValue: 'Somali', value: 'so' },
  { viewValue: 'Albanian', value: 'sq' },
  { viewValue: 'Serbian', value: 'sr' },
  { viewValue: 'Swati', value: 'ss' },
  { viewValue: 'Sotho, Southern', value: 'st' },
  { viewValue: 'Sundanese', value: 'su' },
  { viewValue: 'Swedish', value: 'sv' },
  { viewValue: 'Swahili', value: 'sw' },
  { viewValue: 'Tamil', value: 'ta' },
  { viewValue: 'Telugu', value: 'te' },
  { viewValue: 'Tajik', value: 'tg' },
  { viewValue: 'Thai', value: 'th' },
  { viewValue: 'Tigrinya', value: 'ti' },
  { viewValue: 'Turkmen', value: 'tk' },
  { viewValue: 'Tagalog', value: 'tl' },
  { viewValue: 'Tswana', value: 'tn' },
  { viewValue: 'Tonga (Tonga Islands)', value: 'to' },
  { viewValue: 'Turkish', value: 'tr' },
  { viewValue: 'Tsonga', value: 'ts' },
  { viewValue: 'Tatar', value: 'tt' },
  { viewValue: 'Twi', value: 'tw' },
  { viewValue: 'Tahitian', value: 'ty' },
  { viewValue: 'Uighur; Uyghur', value: 'ug' },
  { viewValue: 'Ukrainian', value: 'uk' },
  { viewValue: 'Urdu', value: 'ur' },
  { viewValue: 'Uzbek', value: 'uz' },
  { viewValue: 'Venda', value: 've' },
  { viewValue: 'Vietnamese', value: 'vi' },
  { viewValue: 'Volap\u00fck', value: 'vo' },
  { viewValue: 'Walloon', value: 'wa' },
  { viewValue: 'Wolof', value: 'wo' },
  { viewValue: 'Xhosa', value: 'xh' },
  { viewValue: 'Yiddish', value: 'yi' },
  { viewValue: 'Yoruba', value: 'yo' },
  { viewValue: 'Zhuang; Chuang', value: 'za' },
  { viewValue: 'Chinese', value: 'zh' },
  { viewValue: 'Zulu', value: 'zu' },
];

const categories: Option[] = [
  { viewValue: 'Study', value: 's' },
  { viewValue: 'Work', value: 'w' },
];

const currencies: Option[] = [
  {
    value: 'AED',
    viewValue: 'United Arab Emirates Dirham',
  },
  {
    value: 'AFN',
    viewValue: 'Afghan Afghani',
  },
  {
    value: 'ALL',
    viewValue: 'Albanian Lek',
  },
  {
    value: 'AMD',
    viewValue: 'Armenian Dram',
  },
  {
    value: 'ANG',
    viewValue: 'Netherlands Antillean Guilder',
  },
  {
    value: 'AOA',
    viewValue: 'Angolan Kwanza',
  },
  {
    value: 'ARS',
    viewValue: 'Argentine Peso',
  },
  {
    value: 'AUD',
    viewValue: 'Australian Dollar',
  },
  {
    value: 'AWG',
    viewValue: 'Aruban Florin',
  },
  {
    value: 'AZN',
    viewValue: 'Azerbaijani Manat',
  },
  {
    value: 'BAM',
    viewValue: 'Bosnia-Herzegovina Convertible Mark',
  },
  {
    value: 'BBD',
    viewValue: 'Barbadian Dollar',
  },
  {
    value: 'BDT',
    viewValue: 'Bangladeshi Taka',
  },
  {
    value: 'BGN',
    viewValue: 'Bulgarian Lev',
  },
  {
    value: 'BHD',
    viewValue: 'Bahraini Dinar',
  },
  {
    value: 'BIF',
    viewValue: 'Burundian Franc',
  },
  {
    value: 'BMD',
    viewValue: 'Bermudan Dollar',
  },
  {
    value: 'BND',
    viewValue: 'Brunei Dollar',
  },
  {
    value: 'BOB',
    viewValue: 'Bolivian Boliviano',
  },
  {
    value: 'BRL',
    viewValue: 'Brazilian Real',
  },
  {
    value: 'BSD',
    viewValue: 'Bahamian Dollar',
  },
  {
    value: 'BTC',
    viewValue: 'Bitcoin',
  },
  {
    value: 'BTN',
    viewValue: 'Bhutanese Ngultrum',
  },
  {
    value: 'BWP',
    viewValue: 'Botswanan Pula',
  },
  {
    value: 'BYN',
    viewValue: 'Belarusian Ruble',
  },
  {
    value: 'BZD',
    viewValue: 'Belize Dollar',
  },
  {
    value: 'CAD',
    viewValue: 'Canadian Dollar',
  },
  {
    value: 'CDF',
    viewValue: 'Congolese Franc',
  },
  {
    value: 'CHF',
    viewValue: 'Swiss Franc',
  },
  {
    value: 'CLF',
    viewValue: 'Chilean Unit of Account (UF)',
  },
  {
    value: 'CLP',
    viewValue: 'Chilean Peso',
  },
  {
    value: 'CNH',
    viewValue: 'Chinese Yuan (Offshore)',
  },
  {
    value: 'CNY',
    viewValue: 'Chinese Yuan',
  },
  {
    value: 'COP',
    viewValue: 'Colombian Peso',
  },
  {
    value: 'CRC',
    viewValue: 'Costa Rican Colón',
  },
  {
    value: 'CUC',
    viewValue: 'Cuban Convertible Peso',
  },
  {
    value: 'CUP',
    viewValue: 'Cuban Peso',
  },
  {
    value: 'CVE',
    viewValue: 'Cape Verdean Escudo',
  },
  {
    value: 'CZK',
    viewValue: 'Czech Republic Koruna',
  },
  {
    value: 'DJF',
    viewValue: 'Djiboutian Franc',
  },
  {
    value: 'DKK',
    viewValue: 'Danish Krone',
  },
  {
    value: 'DOP',
    viewValue: 'Dominican Peso',
  },
  {
    value: 'DZD',
    viewValue: 'Algerian Dinar',
  },
  {
    value: 'EGP',
    viewValue: 'Egyptian Pound',
  },
  {
    value: 'ERN',
    viewValue: 'Eritrean Nakfa',
  },
  {
    value: 'ETB',
    viewValue: 'Ethiopian Birr',
  },
  {
    value: 'EUR',
    viewValue: 'Euro',
  },
  {
    value: 'FJD',
    viewValue: 'Fijian Dollar',
  },
  {
    value: 'FKP',
    viewValue: 'Falkland Islands Pound',
  },
  {
    value: 'GBP',
    viewValue: 'British Pound Sterling',
  },
  {
    value: 'GEL',
    viewValue: 'Georgian Lari',
  },
  {
    value: 'GGP',
    viewValue: 'Guernsey Pound',
  },
  {
    value: 'GHS',
    viewValue: 'Ghanaian Cedi',
  },
  {
    value: 'GIP',
    viewValue: 'Gibraltar Pound',
  },
  {
    value: 'GMD',
    viewValue: 'Gambian Dalasi',
  },
  {
    value: 'GNF',
    viewValue: 'Guinean Franc',
  },
  {
    value: 'GTQ',
    viewValue: 'Guatemalan Quetzal',
  },
  {
    value: 'GYD',
    viewValue: 'Guyanaese Dollar',
  },
  {
    value: 'HKD',
    viewValue: 'Hong Kong Dollar',
  },
  {
    value: 'HNL',
    viewValue: 'Honduran Lempira',
  },
  {
    value: 'HRK',
    viewValue: 'Croatian Kuna',
  },
  {
    value: 'HTG',
    viewValue: 'Haitian Gourde',
  },
  {
    value: 'HUF',
    viewValue: 'Hungarian Forint',
  },
  {
    value: 'IDR',
    viewValue: 'Indonesian Rupiah',
  },
  {
    value: 'ILS',
    viewValue: 'Israeli New Sheqel',
  },
  {
    value: 'IMP',
    viewValue: 'Manx pound',
  },
  {
    value: 'INR',
    viewValue: 'Indian Rupee',
  },
  {
    value: 'IQD',
    viewValue: 'Iraqi Dinar',
  },
  {
    value: 'IRR',
    viewValue: 'Iranian Rial',
  },
  {
    value: 'ISK',
    viewValue: 'Icelandic Króna',
  },
  {
    value: 'JEP',
    viewValue: 'Jersey Pound',
  },
  {
    value: 'JMD',
    viewValue: 'Jamaican Dollar',
  },
  {
    value: 'JOD',
    viewValue: 'Jordanian Dinar',
  },
  {
    value: 'JPY',
    viewValue: 'Japanese Yen',
  },
  {
    value: 'KES',
    viewValue: 'Kenyan Shilling',
  },
  {
    value: 'KGS',
    viewValue: 'Kyrgystani Som',
  },
  {
    value: 'KHR',
    viewValue: 'Cambodian Riel',
  },
  {
    value: 'KMF',
    viewValue: 'Comorian Franc',
  },
  {
    value: 'KPW',
    viewValue: 'North Korean Won',
  },
  {
    value: 'KRW',
    viewValue: 'South Korean Won',
  },
  {
    value: 'KWD',
    viewValue: 'Kuwaiti Dinar',
  },
  {
    value: 'KYD',
    viewValue: 'Cayman Islands Dollar',
  },
  {
    value: 'KZT',
    viewValue: 'Kazakhstani Tenge',
  },
  {
    value: 'LAK',
    viewValue: 'Laotian Kip',
  },
  {
    value: 'LBP',
    viewValue: 'Lebanese Pound',
  },
  {
    value: 'LKR',
    viewValue: 'Sri Lankan Rupee',
  },
  {
    value: 'LRD',
    viewValue: 'Liberian Dollar',
  },
  {
    value: 'LSL',
    viewValue: 'Lesotho Loti',
  },
  {
    value: 'LYD',
    viewValue: 'Libyan Dinar',
  },
  {
    value: 'MAD',
    viewValue: 'Moroccan Dirham',
  },
  {
    value: 'MDL',
    viewValue: 'Moldovan Leu',
  },
  {
    value: 'MGA',
    viewValue: 'Malagasy Ariary',
  },
  {
    value: 'MKD',
    viewValue: 'Macedonian Denar',
  },
  {
    value: 'MMK',
    viewValue: 'Myanma Kyat',
  },
  {
    value: 'MNT',
    viewValue: 'Mongolian Tugrik',
  },
  {
    value: 'MOP',
    viewValue: 'Macanese Pataca',
  },
  {
    value: 'MRO',
    viewValue: 'Mauritanian Ouguiya (pre-2018)',
  },
  {
    value: 'MRU',
    viewValue: 'Mauritanian Ouguiya',
  },
  {
    value: 'MUR',
    viewValue: 'Mauritian Rupee',
  },
  {
    value: 'MVR',
    viewValue: 'Maldivian Rufiyaa',
  },
  {
    value: 'MWK',
    viewValue: 'Malawian Kwacha',
  },
  {
    value: 'MXN',
    viewValue: 'Mexican Peso',
  },
  {
    value: 'MYR',
    viewValue: 'Malaysian Ringgit',
  },
  {
    value: 'MZN',
    viewValue: 'Mozambican Metical',
  },
  {
    value: 'NAD',
    viewValue: 'Namibian Dollar',
  },
  {
    value: 'NGN',
    viewValue: 'Nigerian Naira',
  },
  {
    value: 'NIO',
    viewValue: 'Nicaraguan Córdoba',
  },
  {
    value: 'NOK',
    viewValue: 'Norwegian Krone',
  },
  {
    value: 'NPR',
    viewValue: 'Nepalese Rupee',
  },
  {
    value: 'NZD',
    viewValue: 'New Zealand Dollar',
  },
  {
    value: 'OMR',
    viewValue: 'Omani Rial',
  },
  {
    value: 'PAB',
    viewValue: 'Panamanian Balboa',
  },
  {
    value: 'PEN',
    viewValue: 'Peruvian Nuevo Sol',
  },
  {
    value: 'PGK',
    viewValue: 'Papua New Guinean Kina',
  },
  {
    value: 'PHP',
    viewValue: 'Philippine Peso',
  },
  {
    value: 'PKR',
    viewValue: 'Pakistani Rupee',
  },
  {
    value: 'PLN',
    viewValue: 'Polish Zloty',
  },
  {
    value: 'PYG',
    viewValue: 'Paraguayan Guarani',
  },
  {
    value: 'QAR',
    viewValue: 'Qatari Rial',
  },
  {
    value: 'RON',
    viewValue: 'Romanian Leu',
  },
  {
    value: 'RSD',
    viewValue: 'Serbian Dinar',
  },
  {
    value: 'RUB',
    viewValue: 'Russian Ruble',
  },
  {
    value: 'RWF',
    viewValue: 'Rwandan Franc',
  },
  {
    value: 'SAR',
    viewValue: 'Saudi Riyal',
  },
  {
    value: 'SBD',
    viewValue: 'Solomon Islands Dollar',
  },
  {
    value: 'SCR',
    viewValue: 'Seychellois Rupee',
  },
  {
    value: 'SDG',
    viewValue: 'Sudanese Pound',
  },
  {
    value: 'SEK',
    viewValue: 'Swedish Krona',
  },
  {
    value: 'SGD',
    viewValue: 'Singapore Dollar',
  },
  {
    value: 'SHP',
    viewValue: 'Saint Helena Pound',
  },
  {
    value: 'SLL',
    viewValue: 'Sierra Leonean Leone',
  },
  {
    value: 'SOS',
    viewValue: 'Somali Shilling',
  },
  {
    value: 'SRD',
    viewValue: 'Surinamese Dollar',
  },
  {
    value: 'SSP',
    viewValue: 'South Sudanese Pound',
  },
  {
    value: 'STD',
    viewValue: 'São Tomé and Príncipe Dobra (pre-2018)',
  },
  {
    value: 'STN',
    viewValue: 'São Tomé and Príncipe Dobra',
  },
  {
    value: 'SVC',
    viewValue: 'Salvadoran Colón',
  },
  {
    value: 'SYP',
    viewValue: 'Syrian Pound',
  },
  {
    value: 'SZL',
    viewValue: 'Swazi Lilangeni',
  },
  {
    value: 'THB',
    viewValue: 'Thai Baht',
  },
  {
    value: 'TJS',
    viewValue: 'Tajikistani Somoni',
  },
  {
    value: 'TMT',
    viewValue: 'Turkmenistani Manat',
  },
  {
    value: 'TND',
    viewValue: 'Tunisian Dinar',
  },
  {
    value: 'TOP',
    viewValue: "Tongan Pa'anga",
  },
  {
    value: 'TRY',
    viewValue: 'Turkish Lira',
  },
  {
    value: 'TTD',
    viewValue: 'Trinidad and Tobago Dollar',
  },
  {
    value: 'TWD',
    viewValue: 'New Taiwan Dollar',
  },
  {
    value: 'TZS',
    viewValue: 'Tanzanian Shilling',
  },
  {
    value: 'UAH',
    viewValue: 'Ukrainian Hryvnia',
  },
  {
    value: 'UGX',
    viewValue: 'Ugandan Shilling',
  },
  {
    value: 'USD',
    viewValue: 'United States Dollar',
  },
  {
    value: 'UYU',
    viewValue: 'Uruguayan Peso',
  },
  {
    value: 'UZS',
    viewValue: 'Uzbekistan Som',
  },
  {
    value: 'VEF',
    viewValue: 'Venezuelan Bolívar Fuerte (Old)',
  },
  {
    value: 'VES',
    viewValue: 'Venezuelan Bolívar Soberano',
  },
  {
    value: 'VND',
    viewValue: 'Vietnamese Dong',
  },
  {
    value: 'VUV',
    viewValue: 'Vanuatu Vatu',
  },
  {
    value: 'WST',
    viewValue: 'Samoan Tala',
  },
  {
    value: 'XAF',
    viewValue: 'CFA Franc BEAC',
  },
  {
    value: 'XAG',
    viewValue: 'Silver Ounce',
  },
  {
    value: 'XAU',
    viewValue: 'Gold Ounce',
  },
  {
    value: 'XCD',
    viewValue: 'East Caribbean Dollar',
  },
  {
    value: 'XDR',
    viewValue: 'Special Drawing Rights',
  },
  {
    value: 'XOF',
    viewValue: 'CFA Franc BCEAO',
  },
  {
    value: 'XPD',
    viewValue: 'Palladium Ounce',
  },
  {
    value: 'XPF',
    viewValue: 'CFP Franc',
  },
  {
    value: 'XPT',
    viewValue: 'Platinum Ounce',
  },
  {
    value: 'YER',
    viewValue: 'Yemeni Rial',
  },
  {
    value: 'ZAR',
    viewValue: 'South African Rand',
  },
  {
    value: 'ZMW',
    viewValue: 'Zambian Kwacha',
  },
  {
    value: 'ZWL',
    viewValue: 'Zimbabwean Dollar',
  },
];

const rankTypes: Option[] = [
  { value: 'none', viewValue: 'None' },
  { value: 'rate', viewValue: 'Rate' },
  { value: 'fifa', viewValue: 'Fifa' },
];

const ruleTypes: Option[] = [
  { value: 'j', viewValue: 'Join First' },
  { value: 'p', viewValue: 'Priority' },
];

const eventVisibilities: Option[] = [
  { value: 'P', viewValue: 'Private' },
  { value: 'F', viewValue: 'Friends Only' },
  { value: 'A', viewValue: 'Public' }
];

const eventCategories: Option[] = [
  { value: 'g', viewValue: 'General' }
];

const eventTypes: Option[] = [
  { value: 'E', viewValue: 'Event' },
  { value: 'P', viewValue: 'Promotion' },
  { value: 'T', viewValue: 'Idea' }, //=> Template
];

const statuses: Option[] = [
  { value: 'I', viewValue: 'Invisible' },
  //{ value: 'F', viewValue: 'Friends Only' },
  //{ value: 'A', viewValue: 'All' },
  { value: 'A', viewValue: 'Active' },
];

const groupCategories: Option[] = [
  { value: 'g', viewValue: 'Generic' },
];

const groupTypes: Option[] = [
  { value: 'i', viewValue: 'Separate Profile' },
  { value: 's', viewValue: 'Sub Group' },
];

const groupVisibilities: Option[] = [
  { value: 'pr', viewValue: 'Private' },
  // { value: 'b', viewValue: 'Business' },
  { value: 'pu', viewValue: 'Public' },
];

const roles: Option[] = [
  { value: 'ROLE_ADMIN', viewValue: 'Admin' },
  { value: 'ROLE_USER', viewValue: 'User' },
];

const profileStatuses: Option[] = [
  { value: 'P', viewValue: 'Pending' },
  { value: 'A', viewValue: 'All' },
  { value: 'F', viewValue: 'Friends Only' },
  { value: 'S', viewValue: 'Suspended' },
];

const templateTypes: Option[] = [
  { value: 'e', viewValue: 'Event' },
  { value: 'j', viewValue: 'Job' },
  { value: 'i', viewValue: 'Idea' },
];

// https://www.techiediaries.com/angular-11-tutorial-example-rest-crud-http-get-httpclient/
@Injectable({
  providedIn: 'any',
})
export class DataService {
  get professions(): Option[] {
    return professions;
  }

  get physiques(): Option[] {
    return physiques;
  }

  get religions(): Option[] {
    return religions;
  }

  get languages(): Option[] {
    return languages;
  }

  get categories(): Option[] {
    return categories;
  }

  get statuses(): Option[] {
    return statuses;
  }

  get eventVisibilities(): Option[] {
    return eventVisibilities;
  }

  get eventCategories(): Option[] {
    return eventCategories;
  }

  get eventTypes(): Option[] {
    return eventTypes;
  }

  get ruleTypes(): Option[] {
    return ruleTypes;
  }

  get rankTypes(): Option[] {
    return rankTypes;
  }

  get templateTypes(): Option[] {
    return templateTypes;
  }

  get currencies(): Option[] {
    return currencies;
  }

  get groupCategories(): Option[] {
    return groupCategories;
  }

  get groupTypes(): Option[] {
    return groupTypes;
  }

  get groupVisibilities(): Option[] {
    return groupVisibilities;
  }

  get roles(): Option[] {
    return roles;
  }

  get profileStatuses(): Option[] {
    return profileStatuses;
  }
}
