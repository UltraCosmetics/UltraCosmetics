package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.hats.Hat;
import be.isach.ultracosmetics.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * Hat types.
 *
 * @author iSach
 * @since 10-15-2015
 */
public class HatType extends CosmeticType<Hat> {
    private static final String HAT_NAME = ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Hat";
    /**
     * The HatType ItemStack
     */
    private final ItemStack itemStack;

    /**
     * Standard skull hats
     */
    private HatType(String texture, String configName) {
        super(Category.HATS, configName, XMaterial.PLAYER_HEAD, Hat.class);
        this.itemStack = ItemFactory.createSkull(texture, HAT_NAME);
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".Name", configName);
        }
    }

    /**
     * Used for carved pumpkins and such
     */
    private HatType(ItemStack item, String configName) {
        super(Category.HATS, configName, XMaterial.matchXMaterial(item.getType()), Hat.class);
        this.itemStack = item;
        if (GENERATE_MISSING_MESSAGES) {
            MessageManager.addMessage(getConfigPath() + ".Name", configName);
        }
    }

    /**
     * Gets the HatType ItemStack.
     *
     * @return the HatType ItemStack.
     */
    @Override
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * Gets the HatType Name (in Menu).
     *
     * @return the HatType Name (in Menu).
     */
    @Override
    public Component getName() {
        return MessageManager.getMessage("Hats." + getConfigName() + ".Name");
    }

    public static void register() {
        new HatType("3e8aad673157c92317a88b1f86f5271f1cd7397d7fc8ec3281f733f751634", "Astronaut");
        new HatType("636e26c44659e8148ed58aa79e4d60db595f426442116f81b5415c2446ed8", "Scared");
        new HatType("3e1debc73231f8ed4b69d5c3ac1b1f18f3656a8988e23f2e1bdbc4e85f6d46a", "Angel");
        new HatType("f720df911c052377065408db78a25c678f791eb944c063935ae86dbe51c71b", "Embarassed");
        new HatType("545bd18a2aaf469fad72e52cde6cfb02bfbaa5bfed2a8151277f779ebcdcec1", "Kissy");
        new HatType("14968ac5af3146826fa2b0d4dd114fda197f8b28f4750553f3f88836a21fac9", "Sad");
        new HatType("868f4cef949f32e33ec5ae845f9c56983cbe13375a4dec46e5bbfb7dcb6", "Cool");
        new HatType("bc2b9b9ae622bd68adff7180f8206ec4494abbfa130e94a584ec692e8984ab2", "Surprised");
        new HatType("b371e4e1cf6a1a36fdae27137fd9b8748e6169299925f9af2be301e54298c73", "Dead");
        new HatType("1f1b875de49c587e3b4023ce24d472ff27583a1f054f37e73a1154b5b5498", "Crying");
        new HatType("5059d59eb4e59c31eecf9ece2f9cf3934e45c0ec476fc86bfaef8ea913ea710", "BigSmile");
        new HatType("f4ea2d6f939fefeff5d122e63dd26fa8a427df90b2928bc1fa89a8252a7e", "Wink");
        new HatType("3baabe724eae59c5d13f442c7dc5d2b1c6b70c2f83364a488ce5973ae80b4c3", "Derp");
        new HatType("52e98165deef4ed621953921c1ef817dc638af71c1934a4287b69d7a31f6b8", "Smile");
        new HatType("bba8459145d83ffc44ad58c3260e74ca5a0f634c7eeb59a1ad3234849c933c", "Iron");
        new HatType("b6d1ce697e9dbaa4ccf642516aaa5981332dac1d331afee2ee3dcc89efdedb", "Gold");
        new HatType("c01461973634525196ecc757693b171ada4ef24aa92836f42ea11bd79c3a502d", "Diamond");
        new HatType("aa868ce917c09af8e4c350a5807041f6509bf2b89aca45e591fbbd7d4b117d", "Piston");
        new HatType("8514d225b262d847c7e557b474327dcef758c2c5882e41ee6d8c5e9cd3bc914", "CommandBlock");
        new HatType("4ceeb77d4d25724a9caf2c7cdf2d88399b1417c6b9ff5213659b653be4376e3", "Music");
        new HatType("01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac", "Squid");
        new HatType("9209e11e61c727cfa6dc128451e412f301d127173c5e52a0b1a9a2439d76b4e", "GlowSquid");
        new HatType("1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893", "Chicken");
        new HatType("621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4", "Pig");
        new HatType("d6551840955f524367580f11b35228938b6786397a8f2e8c8cc6b0eb01b5db3d", "Cow");
        new HatType("767ac842a8d12c02d8a9f0d803eda918dc4d0c80e0f2ea02b4b9a7581cd7a4b5", "Mooshroom");
        new HatType("199cd80c0a353b181b6588e9d820671c59ed9f27f1cfcd2195e65b918fb65e47", "BrownMooshroom");
        new HatType("e1908275fba4fce37521323bc889b727a6692d241b50f2b072576056424decf6", "Moobloom");
        new HatType("f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70", "Sheep");
        new HatType("a996399fff9cbcfb7ba677dd0c2d104229d1cc2307a6f075a882da4694ef80ae", "ChestnutHorse");
        new HatType("89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714", "Golem");
        new HatType("cec35e56e50f0f47750e9f4c95dd216eaad1e774e46de3d6c9e0befeaca7e6c9", "CopperGolem");
        new HatType("7a59bb0a7a32965b3d90d8eafa899d1835f424509eadd4e6b709ada50b9cf", "Enderman");
        new HatType("d685e2e906b44647a9cdc8066b106bc50a2c9abf974e69239917fd21a15b4368", "EndermanKing");
        new HatType("61affd31efc37ba84f50187394d8688344ccd06cdc926ddfcf2df116986dca9", "Slime");
        new HatType("f4254838c33ea227ffca223dddaabfe0b0215f70da649e944477f44370ca6952", "Creeper");
        new HatType("301268e9c492da1f0d88271cb492a4b302395f515a7bbf77f4a20b95fc02eb2", "Skeleton");
        new HatType("56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11", "Zombie");
        new HatType("c84df79c49104b198cdad6d99fd0d0bcf1531c92d4ab6269e40b7d3cbbb8e98c", "Drowned");
        new HatType("b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0", "Blaze");
        new HatType("65ccbb547820b667cc9d3bc9fff1e3d65da2375655a3427b30e1d009eeb272ce", "Strider");
        new HatType("7953b6c68448e7e6b6bf8fb273d7203acd8e1be19e81481ead51f45de59a8", "WitherSkeleton");
        new HatType("8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0", "Ghast");
        new HatType("6cf3674b2ddc0ef7c39e3b9c6b58677de5cf377d2eb073f2f3fe50919b1ca4c9", "Warden");
        new HatType("fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90", "Witch");
        new HatType("495290e090c238832bd7860fc033948c4d031353533ac8f67098823b7f667f1c", "Guardian");
        new HatType("19e04a752596f939f581930414561b175454d45a0506501e7d2488295a5d5de", "ShepherdVillager");
        new HatType("d831830a7bd3b1ab05beb98dc2f9fc5ea550b3cf649fd94d483da7cd39f7c063", "FletcherVillager");
        new HatType("861f5762a3faf529044cf456054b8e6d6ba8b287308a8a08b287070f8d16efe5", "LibrarianVillager");
        new HatType("4420c9c43e095880dcd2e281c81f47b163b478f58a584bb61f93e6e10a155f31", "Bee");
        new HatType("d8954a42e69e0881ae6d24d4281459c144a0d5a968aed35d6d3d73a3c65d26a", "Fox");
        new HatType("41436377eb4c4b4e39fb0e1ed8899fb61ee1814a9169b8d08729ef01dc85d1ba", "SnowFox");
        new HatType("cbb214a348529a0979574b87b06a480cc6177810f79491ce983f16dc3d844662", "Ocelot");
        new HatType("51a88b69d82dbcc85f00cff8703705f9408bcdd3a61cefbf67aa5efee5d77f30", "Frog");
        new HatType("d704254139a0b1a926e7552482dd67679c6ae0dc8335c980dbd1c0d99634a708", "AxolotlPink");
        new HatType("7595819359c7ed9e979bd53a49b2861620232d6a87eed70687ccd0bf0d3ce8f3", "AxolotlBlue");
        new HatType("0a4050e7aacc4539202658fdc339dd182d7e322f9fbcc4d5f99b5718a", "Turtle");
        new HatType("4b953e4d3dfa1c622f30c4482d9a726ea12592134f1e3bc79e0c0c5d84c36", "Clownfish");
        new HatType("68c2f1f7e8cd6b00d30f0edaeefce38e889173c30c701fac0da860e0a2125ec8", "BlackCat");
        new HatType("e50294a1747310f104124c6373cc639b712baa57b7d926297b645188b7bb9ab9", "Allay");
        new HatType("6681a72da7263ca9aef066542ecca7a180c40e328c0463fcb114cb3b83057552", "Bat");
        new HatType("799f4fcc53c0700ec244b40c85af0eac60cb6f22bdee6fbd86517a456150f995", "Glare");
        new HatType("4b1411e96d872818b59f5b0e9986346e28726012c1f2537203e29ae327ff138a", "Rana");
        new HatType("123fa5a163d4295d3db14ac429f78842ce1b0178f7d27a77e3fdad65c8d74ed6", "MCD-Steve");
        new HatType("a2f71de1fd66e37362cdd86589e9b34e847999e232aa717ebbc86330ffb4f637", "MCD-Alex");
        new HatType("af498ce713d6558c7134d643ce7051414f2f3ab87ad79e40ffd5d47c63dea7a5", "MCD-QuestGiver");
        new HatType("10cd12583f561084e8415401947f65e313218e6e84def5d371d27b6db28f4d16", "MCD-LuxuryMerchant");
        new HatType("e0686a0d939bc9acf1ce1f668d4084e7e64d615553fb1c36f5127b41c8cb7911", "MCD-Necromancer");
        new HatType("3affce65f4236647ecd6d55c127eef8e7e132f43d01432aea360f0b67c8a6677", "MCD-NamelessOne");
        new HatType("7a5c03734be0976fd02092d699a983fee4c7a852e349d9dd01a27afc46e27cf9", "MCD-StrayKnight");
        new HatType("13dda8c603e11c15c370a7bbf8d989bc0be6a261394834bc291536decf3d83ba", "MCD-AncientGuardian");
        new HatType("4a307624f5604c25d1c156e5b9d1afc3a6af63286e3975ba898b64d0c0eabee9", "MCD-KeyGolem");
        new HatType("5901ebae71e9d45ba7372f612e385b38fb19105ac1db5008a58b26dfe021f6d7", "MCD-ArchIllager");
        new HatType("74bf5321056cd7c0672dedd3e24f4b0b171db25fc451c4ae0bc8036be0dbd262", "MCD-SunkenSkeleton");
        new HatType("383447a99d5db4ee950d995a2a4a2fc32eda0106a7d6b599c12f4a47dd30d769", "MCD-TheWildfire");
        new HatType("dba8d8e53d8a5a75770b62cce73db6bab701cc3de4a9b654d213d54af9615", "Mario");
        new HatType("ff1533871e49ddab8f1ca82edb1153a5e2ed3764fd1ce029bf829f4b3caac3", "Luigi");
        new HatType("f256f71735ef458581c9dacf394185eed9b33cb6ec5cd594a57153a8b566560", "Batman");
        new HatType("6f68d509b5d1669b971dd1d4df2e47e19bcb1b33bf1a7ff1dda29bfc6f9ebf", "Chest");
        new HatType("11f54ff9bb42851912aa87a1bda5b7cd9814ccccfbe225fdda887ad6180d9", "Skull");
        new HatType("68d2183640218ab330ac56d2aab7e29a9790a545f691619e38578ea4a69ae0b6", "Ghost");
        new HatType("0289d4b4c96295915f068b99c27d394273f9f264fc968c5d5c47df2f5be2", "JackOLantern");
        new HatType("86dbc1debc57438a5de4ba915151382abc3d8f1318e2a35e78dfb30f04bc467", "ScaryClown");
        new HatType("2d61ccbfdcdf8941adaf76c6c0e0182d2c8bbb5dc18f374895652bc661b6ed", "Santa");
        new HatType("98e334e4bee04264759a766bc1955cfaf3f56201428fafec8d4bf1bb36ae6", "Snowman");
        new HatType("f0afa4fffd10863e76c698da2c9c9e799bcf9ab9aa37d8312881734225d3ca", "Present");
        new HatType("82ab6c79c63b8334b2c03b6f736acf61aced5c24f2ba72b777d77f28e8c", "Elf");
        new HatType("36d1fabdf3e342671bd9f95f687fe263f439ddc2f1c9ea8ff15b13f1e7e48b9", "Bedrock");
        new HatType("a7d5eb0aea5d61ba3ff4996416a90096a9d77609ebcd3b308f906ae888a45f6d", "RedCrewmate");
        new HatType("6670bc5f045830094054aebc75b2ed37fc55f524d979d81ef61f3de5c217d0ca", "BlueCrewmate");
        new HatType("4e633480d4bfbeaa049d013ed5746d9f5df9495d0bae1d9a70d5e2649bc264f", "GreenCrewmate");
        new HatType("7d3ef1564636889fe3acd3bb264efd752c90d4c6b23b00a3ed6c2d7f5e822775", "CyanCrewmate");
        new HatType("e58e56c765e34423ad2877840ab7c5688b44939c537c202363a4f1b5b7580dc8", "LimeCrewmate");
        new HatType("ab6b12c1b862b68936e8aee7a248c3e252e88b1fcff05700fce1c959120a229d", "YellowCrewmate");
        new HatType("d910e30441cb829b4ee8ca1c0444c1fac6d94ace5a5c17ce46d4ef6cd93b23a9", "OrangeCrewmate");
        new HatType("68b818677be3c2079937137f50d555c161703d07e99cc708b8b5f4112938281", "PurpleCrewmate");
        new HatType("24e95bdd5151222561370bb67ad4bb0366410f9186dd00ca4d45c6feb8419eac", "BlackCrewmate");
        new HatType("e994f7b302612ac3231d41f0e6d78a3082db3bd667d0a9c5bcf12ced8f9405bc", "WhiteCrewmate");
        new HatType("feb20b93453a82018e2d4063b084035a5fe55a8a175da4ce1adbc6ec40ebe272", "PinkCrewmate");
        new HatType("c359cc1b468bba51707d0b6e4d229da550bcd8bcbd4fcff2720540a85681b17b", "BrownCrewmate");
        new HatType("89556655788a075510b1958a38e3fab4b9b3baa8e55601d28ed79bb9426a2996", "FrogPrince");
        new HatType("e0b59438013ea95362c6f1224b7c5bb6c2792b0b9c9cefd6d708767fd91ec", "Notch");
        new HatType("ee3024011134a74e9f79a22ba8e7672525fb4bf65e108a77a4aeca2bce03b903", "KitsuneMask");
        new HatType("a5e930ae463048a73e6e9630c9accbf7195e30e80f224107a26e407fca319287", "Goblin");
        new HatType("eeb2054365192912d2c88d0f5b8da676bc588eac51ffd6ea0bf67b492b3b6a0a", "Unicorn");
        new HatType("2f9f51b4fbb43d6f9b5a54ffd6d392c228afa5162633e84827ab0b1515c8423e", "Ogre");
        new HatType("8d2d11356359b5ac6bea7f3c5b3b40278e14397c100b070bfe9967eba3a95263", "GreenDragon");
        new HatType("d1572ddb5a0e1cc94aab01e972e41fdfcb2f088437f5338927fe1cc6e1da716f", "GigaChad");
        new HatType("f0f9f5126a6c7023ec08c9f9f0808adab713aca812ecbc025a056052ba505a40", "Anubis");
        new HatType("f3487d457f9062d787a3e6ce1c4664bf7402ec67dd111256f19b38ce4f670", "Bread");
        new HatType("955d611a878e821231749b2965708cad942650672db09e26847a88e2fac2946", "Cheese");
        new HatType("347f4f5a74c6691280cd80e7148b49b2ce17dcf64fd55368627f5d92a976a6a8", "Pancakes");
        new HatType("f9136514f342e7c5208a1422506a866158ef84d2b249220139e8bf6032e193", "Cake");
        new HatType("b592cf9f42a5a8c995968493fdd1b11e0b69aad6473ff45384abe58b7fc7c7", "Cookie");
        new HatType("4cc3f781c923a2887f14c1eea11050166966f2602578401f1451e6097b979df", "CandyCane");
        new HatType("819f948d17718adace5dd6e050c586229653fef645d7113ab94d17b639cc466", "Chocolate");
        new HatType("1ed55260dccc8da59338c75e41d544a2e1e7dbef31a69fe42c01b3298bf2d", "WhiteChocolate");
        new HatType("cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633", "Apple");
        new HatType("c3fed514c3e238ca7ac1c94b897ff6711b1dbe50174afc235c8f80d029", "Melon");
        new HatType("fec415d702f3292a82f1471c8794cf63122d449d28ab886d4dc58fafd66", "CarvedPumpkin");
        new HatType("cbc826aaafb8dbf67881e68944414f13985064a3f8f044d8edfb4443e76ba", "Strawberry");
        new HatType("e9b0e969cf3fcced36b712350ffb46d8ed761fe5efb10e3b6a9795e6656da97", "Coconut");
        new HatType("98ced74a22021a535f6bce21c8c632b273dc2d9552b71a38d57269b3538cf", "Taco");
        new HatType("e7ba22d5df21e821a6de4b8c9d373a3aa187d8ae74f288a82d2b61f272e5", "Bacon");
        new HatType("a0eacac41a9eaf051376ef2f959701e1bbe1bf4aa6715adc34b6dc29a13ea9", "Fries");
        new HatType("a6ef1c25f516f2e7d6f7667420e33adcf3cdf938cb37f9a41a8b35869f569b", "Hamburger");
        new HatType("1497b147cfae52205597f72e3c4ef52512e9677020e4b4fa7512c3c6acdd8c1", "Popcorn");
        new HatType("d07b8c51acec2a508bb2fa652fb6e4a08b19485159a099f5982ccb88df1fe27e", "WhiteDonut");
        new HatType("837c9b82b186656e9f6363a2a1c6a4b5b93cfa9ef4dad6f16b94ebb5e362678", "PinkDonut");
        new HatType("59da54ff366e738e31de92919986abb4d50ca944fa9926af63758b7448f18", "ChocolateDonut");
        new HatType("d53c1e87e537f1ab2774ddafb83439b336f4a777b47ad82bcb30d5fcbdf9bc", "Pie");
        new HatType("9c60da2944a177dd08268fbec04e40812d1d929650be66529b1ee5e1e7eca", "A");
        new HatType("8041f5e86983d36eaec4e167b2bbb5a3727607cde88f7555ca1b522a039bb", "B");
        new HatType("d945996c8ae91e376196d4dc676fec31feac790a2f195b2981a703ca1d16cb6", "C");
        new HatType("1641150f481e8492f7128c948996254d2d91fc90f5a8ff4d8ac5c39a6a88a", "D");
        new HatType("db251487ff8eef2ebc7a57dab6e3d9f1db7fc926ddc66fea14afe3dff15a45", "E");
        new HatType("7e433656b443668ed03dac8c442722a2a41221be8bb48e23b35bd8c2e59f63", "F");
        new HatType("995863b73637605feacbb173b77d5e155e65204c78d5c7911f738f28deb60", "G");
        new HatType("3c1d358d927074289cc26bff5b1240746f9f4f0cc46f942f5981c6595f72dd", "H");
        new HatType("8f2295865bda4e47979d36b8a887a75a13b034e6988f78670b64a1e6442c", "I");
        new HatType("e34462b55d7f5823680ad13f2adbd7d1ed46ba5101017ed4b37aeeeb775d", "J");
        new HatType("773325a935c067b6ef227367f62ca4bf49f67adb9f6da32091e2d32c5dde328", "K");
        new HatType("25a1e3328c571aa495d9c5f494815cca176c3acb184feb5a7b9c96ce8e52fce", "L");
        new HatType("d467bf6be95e5c8e9d01977a2f0c487ed5b0de5c87963a2eb15411c442fb2b", "M");
        new HatType("823e434d6395fe7e63492431bdee5782bd5ee5bc8cab7559467bdd1f93b925a", "N");
        new HatType("88445466bdc5ad5bcea82239c4e1b510f6ea5262d82d8a96d7291c342fb89", "O");
        new HatType("f9de601dee3ffeca4d54595f844201d0ed2091acec4548c696bb16a8a158f6", "P");
        new HatType("66ca769bde25d4cc41e19e42adc35ab4c1557b76af232649acc9967ff198f13", "Q");
        new HatType("67a188805162ca5dd4f4649c661d3f6d23c42662aef01645b1a97f78b3f13219", "R");
        new HatType("60d09dfd9f5de6243233e0e3325b6c3479335e7ccf13f2448d4e1f7fc4a0df", "S");
        new HatType("64c75619b91d241f678350ad9237c134c5e08d87d6860741ede306a4ef91", "T");
        new HatType("e9f6d2c6d5285f882ae55d1e91b8f9efdfc9b377208bf4c83f88dd156415e", "U");
        new HatType("dce27a153635f835237d85c6bf74f5b1f2e638c48fee8c83038d0558d41da7", "V");
        new HatType("aedcf4ffcb53b56d42baac9d0dfb118e343462327442dd9b29d49f50a7d38b", "W");
        new HatType("83618ff1217640bec5b525fa2a8e671c75d2a7d7cb2ddc31d79d9d895eab1", "X");
        new HatType("d9c1d29a38bcf113b7e8c34e148a79f9fe41edf41aa8b1de873bb1d433b3861", "Y");
        new HatType("b9295734195d2c7fa389b98757e9686ce6437c16c58bdf2b4cd538389b5912", "Z");

        ConfigurationSection hats = getCustomConfig(Category.HATS);
        if (hats == null) return;
        for (String key : hats.getKeys(false)) {
            ConfigurationSection hat = hats.getConfigurationSection(key);
            if (hat == null) {
                continue;
            }
            if (hat.isString("type") || hat.isString("url")) {
                migrateToXseries(hat);
            }
            ItemStack item;
            try {
                item = ItemFactory.getItemDeserializer().withConfig(hat).read();
            } catch (RuntimeException e) {
                UltraCosmeticsData.get().getPlugin().getLogger().log(Level.WARNING, "Failed to parse custom hat '" + key + "'", e);
                continue;
            }
            addCustomStrings(key);
            new HatType(item, key);
        }
    }

    private static void migrateToXseries(ConfigurationSection hat) {
        if (hat.isString("type")) {
            hat.set("material", hat.getString("type"));
            hat.set("type", null);
        }
        if (hat.isString("url")) {
            hat.set("skull", hat.getString("url"));
            hat.set("url", null);
            // Hat type wasn't previously required, so if we only have a URL, add the material too
            if (!hat.isString("material")) {
                hat.set("material", XMaterial.PLAYER_HEAD.name());
            }
        }
        // custom-model-data is already the correct key so we don't migrate it
        rewriteCustomConfig = true;
    }

    private static void addCustomStrings(String key) {
        MessageManager.addMessage(Category.HATS.getConfigPath() + "." + key + ".Name", key);
        MessageManager.addMessage(Category.HATS.getConfigPath() + "." + key + ".Description", "A custom hat!");
    }
}
