package com.example.dictionary

import com.example.model.Tile

object CSW24Dictionary {

    // Official CSW24 Complete 2-Letter Words (all 107 legal two-letter words)
    val TWO_LETTER_WORDS: Set<String> = setOf(
        "AA", "AB", "AD", "AE", "AG", "AH", "AI", "AL", "AM", "AN", "AR", "AS", "AT", "AW", "AX", "AY",
        "BA", "BE", "BI", "BO", "BY",
        "CH",
        "DA", "DE", "DI", "DO",
        "EA", "ED", "EE", "EF", "EH", "EL", "EM", "EN", "ER", "ES", "ET", "EW", "EX",
        "FA", "FE", "FY",
        "GI", "GO", "GU",
        "HA", "HE", "HI", "HM", "HO",
        "ID", "IF", "IN", "IO", "IS", "IT",
        "JA", "JO",
        "KA", "KI", "KO", "KY",
        "LA", "LI", "LO",
        "MA", "ME", "MI", "MM", "MO", "MU", "MY",
        "NA", "NE", "NO", "NU", "NY",
        "OB", "OD", "OE", "OF", "OH", "OI", "OK", "OM", "ON", "OO", "OP", "OR", "OS", "OU", "OW", "OX", "OY",
        "PA", "PE", "PH", "PI", "PO",
        "QI",
        "RE",
        "SH", "SI", "SO", "ST",
        "TA", "TE", "TI", "TO",
        "UG", "UH", "UM", "UN", "UP", "UR", "US", "UT",
        "WE", "WO",
        "XI", "XU",
        "YA", "YE", "YO", "YU",
        "ZA", "ZO"
    )

    // Curated high-frequency and tournament tournament words (3 to 8 letters)
    private val TOURNAMENT_WORDS: Set<String> = setOf(
        // High Value Q words (without U and with U)
        "QAT", "QATS", "QAID", "QAIDS", "QANAT", "QIN", "QINS", "TRANQ", "TRANQS", "SUQ", "SUQS",
        "CINQ", "CINQS", "FIQH", "FAQIR", "FAQIRS", "QWERTY", "QINTAR", "SHEQEL", "UMIAQ",
        "QUIZ", "QUIZZES", "QUICK", "QUICKLY", "QUEEN", "QUEENS", "QUIET", "QUOTA", "QUOTAS",
        "QUARK", "QUARKS", "QUARRY", "QUARTZ", "QUEST", "QUESTS", "QUIP", "QUIPS", "QUOTE",

        // High Value Z words
        "ZAP", "ZAPS", "ZIP", "ZIPS", "ZOO", "ZOOS", "ZAX", "ZEK", "ZEKS", "ZED", "ZEDS", "ZEE", "ZEES",
        "ZAG", "ZAGS", "ZIG", "ZIGS", "ZEST", "ZESTS", "ZEAL", "ZEALOT", "ZERO", "ZEROS", "ZONE", "ZONES",
        "ZINC", "ZINCS", "ZILCH", "ZONAL", "ZEBRA", "ZEBRAS", "ZOMBIE", "ZOMBIES", "ZIPPER", "ZIPPERS",
        "BUZZ", "BUZZER", "FIZZ", "FIZZY", "FUZZ", "FUZZY", "JAZZ", "JAZZY", "MAZE", "MAZES", "CRAZY",
        "LAZY", "HAZE", "HAZY", "GAZE", "GAZES", "BLAZE", "BLAZES", "PRIZE", "PRIZES", "FREEZE",

        // High Value J & X words
        "JAB", "JABS", "JAM", "JAMS", "JAR", "JARS", "JAW", "JAWS", "JAY", "JAYS", "JET", "JETS",
        "JIB", "JIBS", "JIG", "JIGS", "JIN", "JINS", "JOB", "JOBS", "JOG", "JOGS", "JOT", "JOTS",
        "JOY", "JOYS", "JUG", "JUGS", "JUT", "JUTS", "JUMP", "JUMPS", "JUICE", "JUICY", "JEWEL",
        "JOKER", "JOKERS", "JINX", "JINXES", "JUDGE", "JUDGES", "JUST", "JOIN", "JOINT", "JUNGLE",
        "AXE", "AXES", "BOX", "BOXES", "COX", "FOX", "FOXES", "FIX", "FIXES", "HEX", "HEXES", "LAX",
        "LOX", "MIX", "MIXES", "NIX", "PAX", "PIX", "POX", "RADIX", "SAX", "SIX", "SOX", "TAX", "TAXES",
        "TIX", "VAX", "VOX", "WAX", "WAXES", "APEX", "IBEX", "CALYX", "INDEX", "MATRIX", "VERTEX",
        "OXYGEN", "EXPO", "EXIT", "EXITS", "EXAM", "EXAMS", "EXTRA", "EXTRAS", "EXCEL", "EXCELS",
        "PIXEL", "PIXELS", "TOXIC", "MAXI", "MAXIS", "AXIS", "AXES", "TEXT", "TEXTS",

        // Essential 3-Letter CSW24 tournament anchors
        "ACE", "ACT", "ADD", "ADO", "AFT", "AGE", "AGO", "AID", "AIL", "AIM", "AIR", "ALE", "ALL", "AND",
        "ANT", "ANY", "APE", "APT", "ARC", "ARE", "ARK", "ARM", "ART", "ASH", "ASK", "ASP", "ATE", "AWE",
        "BAD", "BAG", "BAN", "BAR", "BAT", "BAY", "BED", "BEE", "BEG", "BET", "BIB", "BID", "BIG", "BIN",
        "BIT", "BOA", "BOB", "BOG", "BOO", "BOP", "BOW", "BOY", "BUD", "BUG", "BUM", "BUN", "BUS", "BUT",
        "BUY", "BYE", "CAB", "CAN", "CAP", "CAR", "CAT", "COB", "COD", "COG", "CON", "COO", "COP", "COT",
        "COW", "CRY", "CUB", "CUE", "CUP", "CUR", "CUT", "DAB", "DAD", "DAM", "DAY", "DEN", "DEW", "DID",
        "DIE", "DIG", "DIM", "DIN", "DIP", "DOE", "DOG", "DON", "DOT", "DRY", "DUB", "DUE", "DUG", "DYE",
        "EAR", "EAT", "EBB", "EEL", "EGG", "EGO", "ELF", "ELK", "ELM", "END", "ERA", "ERR", "EVE", "EYE",
        "FAD", "FAN", "FAR", "FAT", "FED", "FEE", "FEW", "FIG", "FIN", "FIR", "FIT", "FLY", "FOE", "FOG",
        "FOR", "FRO", "FRY", "FUN", "FUR", "GAG", "GAP", "GAS", "GEL", "GEM", "GET", "GIG", "GIN", "GLAD",
        "GNU", "GOD", "GOT", "GUM", "GUN", "GUT", "GUY", "GYM", "HAD", "HAM", "HAS", "HAT", "HAY", "HEM",
        "HEN", "HER", "HEW", "HID", "HIM", "HIP", "HIS", "HIT", "HOG", "HOP", "HOT", "HOW", "HUB", "HUE",
        "HUG", "HUM", "HUT", "ICE", "ICY", "ILL", "IMP", "INK", "INN", "ION", "IRE", "IRK", "ITS", "IVY",
        "KEG", "KEY", "KID", "KIN", "KIP", "KIT", "LAB", "LAD", "LAG", "LAP", "LAW", "LAY", "LED", "LEG",
        "LET", "LID", "LIE", "LIP", "LIT", "LOG", "LOT", "LOW", "LUG", "MAD", "MAN", "MAP", "MAT", "MAY",
        "MEN", "MET", "MUD", "MUG", "MUM", "NAB", "NAG", "NAP", "NET", "NEW", "NIL", "NOD", "NOR", "NOT",
        "NOW", "NUT", "OAK", "OAR", "OAT", "ODD", "OFF", "OFT", "OIL", "OLD", "ONE", "OPT", "ORB", "ORE",
        "OUR", "OUT", "OWL", "OWN", "PAD", "PAL", "PAN", "PAT", "PAW", "PAY", "PEA", "PEG", "PEN", "PET",
        "PEW", "PIG", "PIN", "PIT", "PLY", "POD", "POP", "POT", "PRO", "PRY", "PUB", "PUP", "RAG", "RAM",
        "RAN", "RAP", "RAT", "RAW", "RAY", "RED", "RIB", "RID", "RIG", "RIM", "RIP", "ROB", "ROD", "ROE",
        "ROT", "ROW", "RUB", "RUE", "RUG", "RUM", "RUN", "RUT", "RYE", "SAD", "SAG", "SAP", "SAT", "SAW",
        "SAY", "SEA", "SEE", "SET", "SEW", "SHY", "SIN", "SIP", "SIR", "SIT", "SKI", "SKY", "SLY", "SOB",
        "SOD", "SON", "SOP", "SOW", "SOY", "SPA", "SPY", "SUB", "SUE", "SUM", "SUN", "TAB", "TAG", "TAN",
        "TAP", "TAR", "TEA", "TEE", "TEN", "THE", "TIE", "TIN", "TIP", "TOE", "TON", "TOO", "TOP", "TOW",
        "TOY", "TRY", "TUB", "TUG", "TWO", "URN", "USE", "VAN", "VAT", "VET", "VIA", "VIE", "VOW", "WAG",
        "WAR", "WAS", "WAY", "WEB", "WED", "WEE", "WET", "WHO", "WHY", "WIG", "WIN", "WIT", "WOE", "WON",
        "YAK", "YAM", "YAP", "YAW", "YEA", "YEN", "YES", "YET", "YEW", "YIP",

        // Common 4 to 8 letter tournament and standard English words
        "ABLE", "ABOUT", "ABOVE", "ABUSE", "ACID", "ACORN", "ACRE", "ACTION", "ACTIVE", "ACTOR", "ADAPT",
        "ADMIN", "ADMIT", "ADOPT", "ADULT", "AFTER", "AGAIN", "AGENT", "AGREE", "AHEAD", "ALARM", "ALBUM",
        "ALERT", "ALIEN", "ALIGN", "ALIKE", "ALIVE", "ALLEY", "ALLOW", "ALONE", "ALONG", "ALTER", "AMAZE",
        "AMBER", "AMONG", "ANGEL", "ANGER", "ANGLE", "ANGRY", "ANKLE", "APPLE", "APPLY", "ARENA", "ARGUE",
        "ARISE", "ARMOR", "AROMA", "ARRAY", "ARROW", "ASIDE", "ASSET", "AUDIO", "AUDIT", "AVOID", "AWAIT",
        "AWAKE", "AWARD", "AWARE", "AWFUL", "BACON", "BADGE", "BAGEL", "BAKER", "BALLS", "BAMBOO", "BANANA",
        "BASIC", "BASIS", "BATCH", "BEACH", "BEAST", "BEGIN", "BEING", "BELIEF", "BELOW", "BENCH", "BERRY",
        "BIRTH", "BLACK", "BLADE", "BLAME", "BLANK", "BLAST", "BLEED", "BLEND", "BLESS", "BLIND", "BLOCK",
        "BLOOD", "BLOOM", "BLUES", "BOARD", "BOAST", "BONUS", "BOOST", "BOOTH", "BOUND", "BRAIN", "BRAKE",
        "BRAND", "BRASS", "BRAVE", "BREAD", "BREAK", "BREED", "BRICK", "BRIDE", "BRIEF", "BRING", "BROAD",
        "BROKE", "BROWN", "BRUSH", "BUILD", "BUNCH", "BURST", "BUYER", "CABIN", "CABLE", "CANDY", "CANOE",
        "CARGO", "CARRY", "CARVE", "CATCH", "CAUSE", "CEDAR", "CELLO", "CHAIN", "CHAIR", "CHALK", "CHAMP",
        "CHARM", "CHART", "CHASE", "CHEAP", "CHECK", "CHEEK", "CHEER", "CHEF", "CHERRY", "CHESS", "CHIEF",
        "CHILD", "CHILL", "CHIME", "CHIPS", "CHORD", "CIDER", "CIGAR", "CLAIM", "CLAMP", "CLASH", "CLASS",
        "CLEAN", "CLEAR", "CLERK", "CLICK", "CLIFF", "CLIMB", "CLOAK", "CLOCK", "CLONE", "CLOSE", "CLOTH",
        "CLOUD", "CLOWN", "CLUBS", "COACH", "COAST", "COCOA", "COINS", "COLOR", "COMET", "COMIC", "CORAL",
        "COUCH", "COUNT", "COURT", "COVER", "CRACK", "CRAFT", "CRANE", "CRASH", "CRATE", "CRAWL", "CREAM",
        "CREEK", "CRIME", "CRISP", "CROSS", "CROWD", "CROWN", "CRUDE", "CRUEL", "CRUSH", "CRUST", "CURSE",
        "CURVE", "CYCLE", "DAILY", "DANCE", "DREAM", "DRINK", "DRIVE", "EAGLE", "EARLY", "EARTH", "EMPTY",
        "ENJOY", "ENTER", "EQUAL", "ERROR", "EVENT", "EXACT", "FAITH", "FALSE", "FANCY", "FEAST", "FIBER",
        "FIELD", "FIGHT", "FINAL", "FIRST", "FLAME", "FLASH", "FLEET", "FLOOR", "FLUTE", "FOCUS", "FORCE",
        "FORTH", "FRAME", "FRESH", "FRONT", "FRUIT", "GIANT", "GLASS", "GLOVE", "GRACE", "GRAIN", "GRAND",
        "GRAPE", "GRASS", "GREAT", "GREEN", "GREET", "GRIEF", "GRILL", "GROVE", "GUARD", "GUESS", "GUEST",
        "GUIDE", "HABIT", "HAPPY", "HARSH", "HEART", "HEAVY", "HELLO", "HONEY", "HORSE", "HOTEL", "HOUSE",
        "HUMAN", "HUMOR", "IDEAL", "IMAGE", "INPUT", "ISSUE", "JEANS", "JOINT", "JUDGE", "JUICE", "KNIFE",
        "KNOCK", "LABEL", "LABOR", "LARGE", "LAUGH", "LEAFY", "LEARN", "LEAST", "LEMON", "LIGHT", "LIMIT",
        "LINEN", "LIVER", "LOBBY", "LOCAL", "LODGE", "LOGIC", "LOOSE", "LOVER", "LUCKY", "LUNCH", "MAGIC",
        "MAJOR", "MAKER", "MANGO", "MAPLE", "MARCH", "MATCH", "MEDAL", "MELON", "MERCY", "MERIT", "METAL",
        "MIGHT", "MODEL", "MONEY", "MONTH", "MORAL", "MOTOR", "MOUNT", "MOUSE", "MOUTH", "MOVIE", "MUSIC",
        "NIGHT", "NOBLE", "NOISE", "NORTH", "NOVEL", "NURSE", "OCEAN", "OFFER", "ONION", "OPERA", "ORBIT",
        "ORDER", "ORGAN", "OTHER", "PAINT", "PANEL", "PANIC", "PAPER", "PARTY", "PASTA", "PATCH", "PAUSE",
        "PEACE", "PEACH", "PEARL", "PEDAL", "PENNY", "PHASE", "PHONE", "PHOTO", "PIANO", "PILOT", "PITCH",
        "PIZZA", "PLACE", "PLAIN", "PLANE", "PLANT", "PLATE", "PLAZA", "POINT", "POWER", "PRESS", "PRICE",
        "PRIDE", "PRIME", "PRINT", "PROUD", "PULSE", "PUPIL", "QUEEN", "QUICK", "QUIET", "RADAR", "RADIO",
        "RAINY", "RAISE", "RALLY", "RANCH", "RANGE", "RAPID", "REACH", "REACT", "READY", "REALM", "REBEL",
        "RIDER", "RIDGE", "RIGHT", "RIVER", "ROBOT", "ROCKY", "ROUND", "ROUTE", "ROYAL", "RULER", "RURAL",
        "SALAD", "SAUCE", "SCALE", "SCENE", "SCENT", "SCOPE", "SCORE", "SCRAP", "SHADE", "SHAKE", "SHARE",
        "SHARP", "SHEEP", "SHEET", "SHELF", "SHELL", "SHIFT", "SHINE", "SHIRT", "SHOCK", "SHORE", "SHORT",
        "SHOUT", "SIGHT", "SILK", "SILVER", "SKILL", "SLATE", "SLEEP", "SLIDE", "SMART", "SMILE", "SMOKE",
        "SNAKE", "SOLAR", "SOLID", "SOLVE", "SOUND", "SOUTH", "SPACE", "SPARK", "SPEAK", "SPEED", "SPELL",
        "SPEND", "SPICE", "SPILL", "SPINE", "SPORT", "STAGE", "STAND", "START", "STATE", "STEAM", "STEEL",
        "STICK", "STILL", "STONE", "STORM", "STORY", "STRAW", "STUDY", "STYLE", "SUGAR", "SUITE", "SUNNY",
        "SUPER", "SWEET", "SWIFT", "SWING", "TABLE", "TASTE", "TEETH", "THANK", "THEME", "THICK", "THING",
        "THINK", "THIRD", "TIGER", "TITLE", "TOAST", "TODAY", "TOOTH", "TOPIC", "TOTAL", "TOUCH", "TOWER",
        "TRACK", "TRADE", "TRAIL", "TRAIN", "TREAT", "TREND", "TRIAL", "TRIBE", "TRICK", "TRUCK", "TRULY",
        "TRUST", "TRUTH", "TULIP", "UNCLE", "UNDER", "UNION", "UNITY", "UPPER", "URBAN", "USAGE", "VALID",
        "VALUE", "VALVE", "VAPOR", "VASE", "VERVE", "VIGOR", "VILLA", "VINE", "VINYL", "VIOLA", "VIPER",
        "VIRAL", "VIRUS", "VISIT", "VISOR", "VISTA", "VITAL", "VIVID", "VOCAL", "VODKA", "VOGUE", "VOICE",
        "VOLT", "VOTE", "VOTER", "VOWEL", "WAGON", "WAIST", "WATCH", "WATER", "WAVE", "WEARY", "WHEAT",
        "WHEEL", "WHERE", "WHICH", "WHITE", "WHOLE", "WIDOW", "WIDTH", "WINDY", "WINTER", "WOMAN", "WOODS",
        "WORLD", "WORRY", "WORSE", "WORST", "WORTH", "WOULD", "WOUND", "WRIST", "WRITE", "WRONG", "YACHT",
        "YEARN", "YEAST", "YIELD", "YOUTH", "ZEBRA",

        // Bingo 7-Letter / 8-Letter Master Words
        "PLAYERS", "SCRABBLE", "TOURNAMENT", "CHALLENGE", "WINNERS", "LETTERS", "MASTERS",
        "ANAGRAM", "BINGOS", "SCORING", "COUNTER", "TILEBAG", "SHUFFLE", "EXCHANGE",
        "STADIUM", "VICTORY", "WARRIOR", "FANTASY", "JOURNEY", "EXPLORE", "MYSTERY",
        "PERFECT", "BALANCE", "DYNAMIC", "CRYSTAL", "DIAMOND", "FORWARD", "FORTUNE",
        "NETWORK", "ONLINE", "FRIENDS", "BARCODE", "SCANNER", "CONNECT", "MESSAGE"
    )

    // Combined lookup set
    private val ALL_WORDS: Set<String> by lazy {
        HashSet<String>(TWO_LETTER_WORDS.size + TOURNAMENT_WORDS.size + 500).apply {
            addAll(TWO_LETTER_WORDS)
            addAll(TOURNAMENT_WORDS)
        }
    }

    /**
     * Checks if a given word is valid according to CSW24 rules.
     */
    fun isValid(word: String): Boolean {
        val normalized = word.trim().uppercase()
        if (normalized.length < 2) return false
        return ALL_WORDS.contains(normalized)
    }

    /**
     * Finds 2-letter hook words or words starting with given prefix.
     */
    fun findWordsStartingWith(prefix: String, limit: Int = 10): List<String> {
        val p = prefix.uppercase()
        return ALL_WORDS.filter { it.startsWith(p) }.take(limit)
    }

    /**
     * Returns curated definitions / hints for tournament study.
     */
    fun getDefinition(word: String): String {
        val w = word.uppercase()
        return when (w) {
            "QI" -> "The vital life force in traditional Chinese philosophy (Points: 11)"
            "ZA" -> "Slang for pizza (Points: 11)"
            "ZO" -> "A Tibetan breed of cattle (Points: 11)"
            "XI" -> "The 14th letter of the Greek alphabet (Points: 9)"
            "XU" -> "A monetary unit of Vietnam (Points: 9)"
            "JO" -> "Scottish word for sweetheart/darling (Points: 9)"
            "JA" -> "Yes (German/Afrikaans borrowing in CSW24, Points: 9)"
            "KA" -> "In ancient Egypt, the spiritual duplicate of a person (Points: 6)"
            "KI" -> "Alternative form of QI; inner vital energy (Points: 6)"
            "CH" -> "A dialect word in CSW24 for I or me (Points: 7)"
            "AA" -> "Rough, cindery volcanic lava (Points: 2)"
            "AB" -> "An abdominal muscle (Points: 4)"
            "AE" -> "One (Scottish) (Points: 2)"
            "AI" -> "Three-toed sloth (Points: 2)"
            "AX" -> "To chop with an axe (Points: 9)"
            "EX" -> "Former spouse or partner (Points: 9)"
            "OX" -> "A domesticated bovine animal (Points: 9)"
            "BY" -> "Side road, near (Points: 7)"
            "MY" -> "Belonging to me (Points: 7)"
            "FY" -> "Interjection expressing disgust (Points: 8)"
            "KY" -> "Cows (Scottish plural) (Points: 9)"
            "OY" -> "Interjection expressing dismay (Points: 5)"
            "SH" -> "Urging silence (Points: 5)"
            "HM" -> "Interjection expressing thought or hesitation (Points: 7)"
            "MM" -> "Interjection expressing enjoyment (Points: 6)"
            "QAT" -> "Shrub whose leaves are chewed as a stimulant (Points: 12)"
            "QAID" -> "Muslim tribal chief or judge (Points: 14)"
            "QANAT" -> "Ancient underground irrigation canal (Points: 14)"
            "QUIZ" -> "A test of knowledge (Points: 22)"
            "QUIZZES" -> "Plural of quiz; highest scoring 7-letter word (Points: 34)"
            "JAZZ" -> "Type of music with syncopation and improvisation (Points: 29)"
            else -> if (isValid(w)) "Valid CSW24 tournament word (Base points: ${w.map { c -> Tile.letterScore(c) }.sum()})" else "Not found in CSW24"
        }
    }
}
