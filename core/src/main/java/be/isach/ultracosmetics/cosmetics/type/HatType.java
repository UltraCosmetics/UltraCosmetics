package be.isach.ultracosmetics.cosmetics.type;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.cosmetics.hats.Hat;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.ItemFactory;
import be.isach.ultracosmetics.util.SmartLogger.LogLevel;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hat types.
 *
 * @author iSach
 * @since 10-15-2015
 */
public class HatType extends CosmeticType<Hat> {

    private static final List<HatType> ENABLED = new ArrayList<>();
    private static final List<HatType> VALUES = new ArrayList<>();
    private static final String URL_PREFIX = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/";
    private static final String URL_SUFFIX = "\"}}}";

    public static List<HatType> enabled() {
        return ENABLED;
    }

    public static List<HatType> values() {
        return VALUES;
    }

    public static HatType valueOf(String s) {
        for (HatType hat : VALUES) {
            if (hat.getConfigName().equalsIgnoreCase(s)) return hat;
        }
        return null;
    }

    public static void checkEnabled() {
        ENABLED.addAll(values().stream().filter(CosmeticType::isEnabled).collect(Collectors.toList()));
    }

    /**
     * The HatType ItemStack
     */
    private final ItemStack itemStack;

    private HatType(String texture, String configName, String defaultDesc) {
        super(Category.HATS, configName, defaultDesc, XMaterial.PLAYER_HEAD, Hat.class);
        String url = URL_PREFIX + texture + URL_SUFFIX;
        String base64 = Base64.getEncoder().encodeToString(url.getBytes());
        this.itemStack = ItemFactory.createSkull(base64, ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Hat");

        VALUES.add(this);
    }

    @Override
    public Hat equip(UltraPlayer player, UltraCosmetics ultraCosmetics) {
        Hat cosmetic = null;
        try {
            cosmetic = getClazz().getDeclaredConstructor(UltraCosmetics.class, UltraPlayer.class, HatType.class).newInstance(ultraCosmetics, player, this);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
        cosmetic.equip();
        return cosmetic;
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
    public String getName() {
        return MessageManager.getMessage("Hats." + getConfigName() + ".Name");
    }

    public static void register() {
        new HatType("3e8aad673157c92317a88b1f86f5271f1cd7397d7fc8ec3281f733f751634", "Astronaut", "&7&oHouston, we have got a problem.");
        new HatType("636e26c44659e8148ed58aa79e4d60db595f426442116f81b5415c2446ed8", "Scared", "&7&oOh god, that scared me!");
        new HatType("3e1debc73231f8ed4b69d5c3ac1b1f18f3656a8988e23f2e1bdbc4e85f6d46a", "Angel", "&7&oDid it hurt when you fell from heaven?");
        new HatType("f720df911c052377065408db78a25c678f791eb944c063935ae86dbe51c71b", "Embarassed", "&7&oI'm kinda embarassed by that.");
        new HatType("545bd18a2aaf469fad72e52cde6cfb02bfbaa5bfed2a8151277f779ebcdcec1", "Kissy", "&7&oWanna kiss?");
        new HatType("14968ac5af3146826fa2b0d4dd114fda197f8b28f4750553f3f88836a21fac9", "Sad", "&7&oI'm so sad.");
        new HatType("868f4cef949f32e33ec5ae845f9c56983cbe13375a4dec46e5bbfb7dcb6", "Cool", "&7&oI'm such a cool guy.");
        new HatType("bc2b9b9ae622bd68adff7180f8206ec4494abbfa130e94a584ec692e8984ab2", "Surprised", "&7&oWow, did not expect that!");
        new HatType("b371e4e1cf6a1a36fdae27137fd9b8748e6169299925f9af2be301e54298c73", "Dead", "&7&oGot rekt");
        new HatType("1f1b875de49c587e3b4023ce24d472ff27583a1f054f37e73a1154b5b5498", "Crying", "&7&oI cry everytime");
        new HatType("5059d59eb4e59c31eecf9ece2f9cf3934e45c0ec476fc86bfaef8ea913ea710", "BigSmile", "&7&oBecause I'm really happy!");
        new HatType("f4ea2d6f939fefeff5d122e63dd26fa8a427df90b2928bc1fa89a8252a7e", "Wink", "&7&oYou know what I mean ;)");
        new HatType("3baabe724eae59c5d13f442c7dc5d2b1c6b70c2f83364a488ce5973ae80b4c3", "Derp", "&7&oDerp Derp Derping all around");
        new HatType("52e98165deef4ed621953921c1ef817dc638af71c1934a4287b69d7a31f6b8", "Smile", "&7&oBecause I'm happy");
        new HatType("bba8459145d83ffc44ad58c3260e74ca5a0f634c7eeb59a1ad3234849c933c", "Iron", "&7&oAs hard as iron!");
        new HatType("b6d1ce697e9dbaa4ccf642516aaa5981332dac1d331afee2ee3dcc89efdedb", "Gold", "&7&oMy precious!");
        new HatType("c01461973634525196ecc757693b171ada4ef24aa92836f42ea11bd79c3a502d", "Diamond", "&7&oThis is really strong!");
        new HatType("aa868ce917c09af8e4c350a5807041f6509bf2b89aca45e591fbbd7d4b117d", "Piston", "&7&oHave you got the redstone?");
        new HatType("8514d225b262d847c7e557b474327dcef758c2c5882e41ee6d8c5e9cd3bc914", "CommandBlock", "&7&oControl the world with it!");
        new HatType("4ceeb77d4d25724a9caf2c7cdf2d88399b1417c6b9ff5213659b653be4376e3", "Music", "&7&oYou are so musical.");
        new HatType("01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac", "Squid", "&7&oBloop Bloop!");
        new HatType("1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893", "Chicken", "&7&oBwwaaaaaaaaaaaakkkkk!");
        new HatType("621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4", "Pig", "&7&oOink Oink!");
        new HatType("d6551840955f524367580f11b35228938b6786397a8f2e8c8cc6b0eb01b5db3d", "Cow", "&7&oempty");
        new HatType("767ac842a8d12c02d8a9f0d803eda918dc4d0c80e0f2ea02b4b9a7581cd7a4b5", "Mooshroom", "&7&oempty");
        new HatType("199cd80c0a353b181b6588e9d820671c59ed9f27f1cfcd2195e65b918fb65e47", "BrownMooshroom", "&7&oempty");
        new HatType("f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70", "Sheep", "&7&oBaaaa, baa");
        new HatType("89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714", "Golem", "&7&oI'm your guard.");
        new HatType("7a59bb0a7a32965b3d90d8eafa899d1835f424509eadd4e6b709ada50b9cf", "Enderman", "&7&oNow I'm here, now I'm there!");
        new HatType("d685e2e906b44647a9cdc8066b106bc50a2c9abf974e69239917fd21a15b4368", "EndermanKing", "&7&oempty");
        new HatType("61affd31efc37ba84f50187394d8688344ccd06cdc926ddfcf2df116986dca9", "Slime", "&7&oempty");
        new HatType("f4254838c33ea227ffca223dddaabfe0b0215f70da649e944477f44370ca6952", "Creeper", "&7&oempty");
        new HatType("301268e9c492da1f0d88271cb492a4b302395f515a7bbf77f4a20b95fc02eb2", "Skeleton", "&7&oempty");
        new HatType("56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11", "Zombie", "&7&oempty");
        new HatType("b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0", "Blaze", "&7&oWatch out for the fire!");
        new HatType("cb7ffdda656c68d88851a8e05b48cd2493773ffc4ab7d64e9302229fe3571059", "Strider", "&7&oempty");
        new HatType("7953b6c68448e7e6b6bf8fb273d7203acd8e1be19e81481ead51f45de59a8", "WitherSkeleton", "&7&oempty");
        new HatType("8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0", "Ghast", "&7&oempty");
        new HatType("6cf3674b2ddc0ef7c39e3b9c6b58677de5cf377d2eb073f2f3fe50919b1ca4c9", "Warden", "&7&oempty");
        new HatType("fce6604157fc4ab5591e4bcf507a749918ee9c41e357d47376e0ee7342074c90", "Witch", "&7&oempty");
        new HatType("d831830a7bd3b1ab05beb98dc2f9fc5ea550b3cf649fd94d483da7cd39f7c063", "FletcherVillager", "&7&oempty");
        new HatType("861f5762a3faf529044cf456054b8e6d6ba8b287308a8a08b287070f8d16efe5", "LibrarianVillager", "&7&oempty");
        new HatType("4420c9c43e095880dcd2e281c81f47b163b478f58a584bb61f93e6e10a155f31", "Bee", "&7&oempty");
        new HatType("d8954a42e69e0881ae6d24d4281459c144a0d5a968aed35d6d3d73a3c65d26a", "Fox", "&7&oempty");
        new HatType("41436377eb4c4b4e39fb0e1ed8899fb61ee1814a9169b8d08729ef01dc85d1ba", "SnowFox", "&7&oempty");
        new HatType("cbb214a348529a0979574b87b06a480cc6177810f79491ce983f16dc3d844662", "Ocelot", "&7&oempty");
        new HatType("51a88b69d82dbcc85f00cff8703705f9408bcdd3a61cefbf67aa5efee5d77f30", "Frog", "&7&oempty");
        new HatType("7595819359c7ed9e979bd53a49b2861620232d6a87eed70687ccd0bf0d3ce8f3", "AxolotlBlue", "&7&oempty");
        new HatType("0a4050e7aacc4539202658fdc339dd182d7e322f9fbcc4d5f99b5718a", "Turtle", "&7&oempty");
        new HatType("4b953e4d3dfa1c622f30c4482d9a726ea12592134f1e3bc79e0c0c5d84c36", "Clownfish", "&7&oempty");
        new HatType("68c2f1f7e8cd6b00d30f0edaeefce38e889173c30c701fac0da860e0a2125ec8", "BlackCat", "&7&oempty");
        new HatType("e50294a1747310f104124c6373cc639b712baa57b7d926297b645188b7bb9ab9", "Allay", "&7&oempty");
        new HatType("6681a72da7263ca9aef066542ecca7a180c40e328c0463fcb114cb3b83057552", "Bat", "&7&oempty");
        new HatType("123fa5a163d4295d3db14ac429f78842ce1b0178f7d27a77e3fdad65c8d74ed6", "MCD-Steve", "&7&oempty");
        new HatType("a2f71de1fd66e37362cdd86589e9b34e847999e232aa717ebbc86330ffb4f637", "MCD-Alex", "&7&oempty");
        new HatType("af498ce713d6558c7134d643ce7051414f2f3ab87ad79e40ffd5d47c63dea7a5", "MCD-QuestGiver", "&7&oempty");
        new HatType("10cd12583f561084e8415401947f65e313218e6e84def5d371d27b6db28f4d16", "MCD-LuxuryMerchant", "&7&oempty");
        new HatType("e0686a0d939bc9acf1ce1f668d4084e7e64d615553fb1c36f5127b41c8cb7911", "MCD-Necromancer", "&7&oempty");
        new HatType("3affce65f4236647ecd6d55c127eef8e7e132f43d01432aea360f0b67c8a6677", "MCD-NamelessOne", "&7&oempty");
        new HatType("dba8d8e53d8a5a75770b62cce73db6bab701cc3de4a9b654d213d54af9615", "Mario", "&7&oIt's-a me! Mario!");
        new HatType("ff1533871e49ddab8f1ca82edb1153a5e2ed3764fd1ce029bf829f4b3caac3", "Luigi", "&7&oLuigi time!");
        new HatType("911be759233c74734b211ece555d585a229a399d5a92b3614ab47c046d785", "DonkeyKong", "&7&oempty");
        new HatType("2743666ea6f8db4b9eefb3b272da9b5e18adccfe5bdc4d7a0d81a42626d4", "DiddyKong", "&7&oempty");
        new HatType("6b5af6a3ccd531ef39cfc9ce8e5ac0d8427dbcd58c6a8de71f83098080fae94e", "FunkyKong", "&7&oempty");
        new HatType("9903ac6dd35c69915a5830e62304870beb6dfbe196a4a03c9d28055f26900aa9", "ShyGuyRed", "&7&oempty");
        new HatType("d89a1d62f2ac7a129e19abf13b17c7af2cbbc756bd669b8e2873767a0ca404", "Link", "&7&oempty");
        new HatType("2fc99dd1e4391cf2fae4f0a95eef4996449a2232d31e4c4b9104221393faf61", "Kirby", "&7&oempty");
        new HatType("f256f71735ef458581c9dacf394185eed9b33cb6ec5cd594a57153a8b566560", "Batman", "&7&oI'm Batman!");
        new HatType("6198f53d4fd976fcd7f087b272dab6c7192fe29ad8ceb8e9e241aca34d8814c7", "Joker", "&7&oempty");
        new HatType("6f68d509b5d1669b971dd1d4df2e47e19bcb1b33bf1a7ff1dda29bfc6f9ebf", "Chest", "&7&oOpen, and close");
        new HatType("11f54ff9bb42851912aa87a1bda5b7cd9814ccccfbe225fdda887ad6180d9", "Skull", "&7&oWho is headless now?");
        new HatType("68d2183640218ab330ac56d2aab7e29a9790a545f691619e38578ea4a69ae0b6", "Ghost", "&7&o2spooky4u");
        new HatType("0289d4b4c96295915f068b99c27d394273f9f264fc968c5d5c47df2f5be2", "JackOLantern", "&7&oA little pumpkin");
        new HatType("86dbc1debc57438a5de4ba915151382abc3d8f1318e2a35e78dfb30f04bc467", "ScaryClown", "&7&oHope you are not scared of clowns.");
        new HatType("2d61ccbfdcdf8941adaf76c6c0e0182d2c8bbb5dc18f374895652bc661b6ed", "Santa", "&7&oHo ho ho! Merry Christmas!");
        new HatType("98e334e4bee04264759a766bc1955cfaf3f56201428fafec8d4bf1bb36ae6", "Snowman", "&7&oI don't have a skull.. or bones");
        new HatType("f0afa4fffd10863e76c698da2c9c9e799bcf9ab9aa37d8312881734225d3ca", "Present", "&7&oFrom Santa, to you!");
        new HatType("82ab6c79c63b8334b2c03b6f736acf61aced5c24f2ba72b777d77f28e8c", "Elf", "&7&oI work for Santa!");
        new HatType("36d1fabdf3e342671bd9f95f687fe263f439ddc2f1c9ea8ff15b13f1e7e48b9", "Bedrock", "&7&oUnbreakable!");
        new HatType("a7d5eb0aea5d61ba3ff4996416a90096a9d77609ebcd3b308f906ae888a45f6d", "RedCrewmate", "&7&oRed sus!");
        new HatType("6670bc5f045830094054aebc75b2ed37fc55f524d979d81ef61f3de5c217d0ca", "BlueCrewmate", "&7&oBlue sus!");
        new HatType("4e633480d4bfbeaa049d013ed5746d9f5df9495d0bae1d9a70d5e2649bc264f", "GreenCrewmate", "&7&oGreen sus!");
        new HatType("7d3ef1564636889fe3acd3bb264efd752c90d4c6b23b00a3ed6c2d7f5e822775", "CyanCrewmate", "&7&oCyan sus!");
        new HatType("e58e56c765e34423ad2877840ab7c5688b44939c537c202363a4f1b5b7580dc8", "LimeCrewmate", "&7&oLime sus!");
        new HatType("ab6b12c1b862b68936e8aee7a248c3e252e88b1fcff05700fce1c959120a229d", "YellowCrewmate", "&7&oYellow sus!");
        new HatType("d910e30441cb829b4ee8ca1c0444c1fac6d94ace5a5c17ce46d4ef6cd93b23a9", "OrangeCrewmate", "&7&oOrange sus!");
        new HatType("68b818677be3c2079937137f50d555c161703d07e99cc708b8b5f4112938281", "PurpleCrewmate", "&7&oPurple sus!");
        new HatType("24e95bdd5151222561370bb67ad4bb0366410f9186dd00ca4d45c6feb8419eac", "BlackCrewmate", "&7&oBlack sus!");
        new HatType("e994f7b302612ac3231d41f0e6d78a3082db3bd667d0a9c5bcf12ced8f9405bc", "WhiteCrewmate", "&7&oWhite sus!");
        new HatType("feb20b93453a82018e2d4063b084035a5fe55a8a175da4ce1adbc6ec40ebe272", "PinkCrewmate", "&7&oPink sus!");
        new HatType("c359cc1b468bba51707d0b6e4d229da550bcd8bcbd4fcff2720540a85681b17b", "BrownCrewmate", "&7&oBrown sus!");
        new HatType("e0e34d31200d5054525008fa116f40c6c62fff600e53032d4140907bbe83972f", "AshKetchum", "&7&oempty");
        new HatType("a77c57ab2e74b616e9f0e92216962640ec8b74f07f0f55c612be0f91dde8950e", "Pikachu", "&7&oempty");
        new HatType("724698da3650717a55f72fa78c14f52c3afbec83e22af9d77504c9c5ede246", "Aladdin", "&7&oempty");
        new HatType("c099119334db7effb3fcb109175c99140e71d69769d97c55fda4bb5047c1", "PrincessJasmine", "&7&oempty");
        new HatType("9d442d7231b79a66b49b7724f44f8f658ed17536fa4f25b353a48f630275f2f8", "Genie", "&7&oempty");
        new HatType("c2e74adfd13dbfc1a3056369edf7cad1c6d637654c7b4819de9afd3a77b14a", "Moana", "&7&oempty");
        new HatType("65a6b036abe28b839782f19eaf32c1e292ab3bf7669564bad0faf2d479e44da6", "Claptrap", "&7&oempty");
        new HatType("93c58218f2075a568ecffe5dfcb79676b32101d7172e15fceda1c066857c43b0", "Sonic", "&7&oempty");
        new HatType("a6bdbcc53c67e970ab3a62143d3eace955f55a589d6372af47db8454ce797883", "HogRider", "&7&oempty");
        new HatType("89556655788a075510b1958a38e3fab4b9b3baa8e55601d28ed79bb9426a2996", "FrogPrince", "&7&oempty");
        new HatType("1c3c470ab7a83fb3f8ab96bae79fd609b27350e0a44cf0818493f32e92af668c", "PussInBoots", "&7&oempty");
        new HatType("b46e15b9dffe2263a5cc44638fb8cdb080e2d5c9d1dda858f3cad7d73cc76e", "Pinocchio", "&7&oempty");
        new HatType("7652d7f3928d476586258b922632251b55b9b40f032645c234d744d15e557", "AliceWonderland", "&7&oempty");
        new HatType("eee42e24a54bf5c3918dacd682c42099e7ccae17a82c783c1fbcc9f009dbeaf0", "TheMadHatter", "&7&oempty");
        new HatType("e0b59438013ea95362c6f1224b7c5bb6c2792b0b9c9cefd6d708767fd91ec", "Notch", "&7&oempty");
        new HatType("ee3024011134a74e9f79a22ba8e7672525fb4bf65e108a77a4aeca2bce03b903", "KitsuneMask", "&7&oempty");
        new HatType("ac8073c19c8311ae7f23c3dd7e411c751250785d69bf835b3963397371912a", "TomNook", "&7&oempty");
        new HatType("1ba4a534402c7bbb8bbbc09849ecf4eab943fe23e694ef174cc93970ef1d4e", "Totoro", "&7&oempty");
        new HatType("589ce948d3172250584aa0e3febc6c1f77a80bbc8e369ecd7d5d0a650bbfa6ab", "NoFace", "&7&oempty");
        new HatType("a5e930ae463048a73e6e9630c9accbf7195e30e80f224107a26e407fca319287", "Goblin", "&7&oempty");
        new HatType("eeb2054365192912d2c88d0f5b8da676bc588eac51ffd6ea0bf67b492b3b6a0a", "Unicorn", "&7&oempty");
        new HatType("2f9f51b4fbb43d6f9b5a54ffd6d392c228afa5162633e84827ab0b1515c8423e", "Ogre", "&7&oempty");
        new HatType("8d2d11356359b5ac6bea7f3c5b3b40278e14397c100b070bfe9967eba3a95263", "GreenDragon", "&7&oempty");
        new HatType("3179b084321cd656c619e8825fbdda92bfebf2e5ad9d2be52a90a53a69c629", "MrMonopoly", "&7&oempty");
        new HatType("bfe00d4de39204036efc7028f22265a3ec5cf9e47b07325f561d1123a4383f1d", "2B", "&7&oempty");
        new HatType("883162ed1526b2f62347efb43748dd4d86dec8baa6efa986878e87d2e2c6", "Plankton", "&7&oempty");
        new HatType("d1572ddb5a0e1cc94aab01e972e41fdfcb2f088437f5338927fe1cc6e1da716f", "GigaChad", "&7&oempty");
        new HatType("88daf54ac8c061a8332697fd038b9f3af8ec38d16849e527125d2973de62e9b", "SpiderMan", "&7&oempty");
        new HatType("49b324b7229e85e5d5e0147e95566f2d7ed16b003c54f46b3122ed1d54e594e5", "IronMan", "&7&oempty");
        new HatType("62bc1a6155c54c965ef23214d0b26b32e133674de774532174d5dbb7753f9000", "Deadpool", "&7&oempty");
        new HatType("6f40db02c784e06427a7fa96e2e8447c63c8e159a0ad2e3e73360a98f51c2d85", "HarryPotter", "&7&oempty");
        new HatType("527c290a5cd58445ddbdc11ea349dfd78e29c24a29fad0fa1847231618dbeb", "AlbusDumbledore", "&7&oempty");
        new HatType("c5ae98138b72ba35da98c8af1be73febc1c858234f2fcccabc02c5a3e9b3f", "Hagrid", "&7&oempty");
        new HatType("fe39af8875c134ea64e6389c8c379e36e514ee67fc552c6d2685d951215a5d24", "LunaLovegood", "&7&oempty");
        new HatType("6324e6eb8dd1d12ed81aea390380d7e566b1a2860fbefba859883d515c5eb218", "DarthVader", "&7&oempty");
        new HatType("3fe78168f7e5c12294adac59f0b9497b6aedc4c323318cabad8dbdbbb22b6", "R2D2", "&7&oempty");
        new HatType("e57137edcc3c82a951abbf8c18ff7189f529245128bdbe29fc6a007a6865be32", "BabyYoda", "&7&oempty");
        new HatType("967e2faeb67b8d1dd3aea3a132d21edd7c8788bb7b07dc088f126fbf601122a9", "Frodo", "&7&oempty");
        new HatType("4a756cfb1dadd53f9f7aead13c386213387159864babcd023dd0d87ba3c86e59", "Bastion", "&7&oempty");
        new HatType("e714f43f1683cc68eae464c411e4bbe72915e6136cb5e0967cd7a23ffec796d3", "DVa", "&7&oempty");
        new HatType("6beb67676ea968c6b543a922f7338d5753ccf43205b7a6de0843ef2d2c8e5c03", "Tracer", "&7&oempty");
        new HatType("3781c252df29412dd4693848e3856ba5ddf116198e5c8748b28b46a85e983", "GeraltOfRivia", "&7&oempty");
        new HatType("f2a0d259aef1cf36cd8c611c38005155b9610127bdd2205a6b5c606de048b0db", "Ciri", "&7&oempty");
        new HatType("af96d2563a5db0999ab0fcdaabd77769dbd491c2b46fbb1bc0b07eaf73dff928", "TrissMerigold", "&7&oempty");
        new HatType("2b193dd1f350aa2d3218c47dfab731b63d6597898d657fa8a58879bc57f3abe", "LaraCroft", "&7&oempty");
        new HatType("239545d4f4099eb836f7e42264104bb7df44e148e442da07302fd7b09f4c1cde", "MaxCaulfield", "&7&oempty");
        new HatType("db505dc7c22bed0620b181e4859d6ec662827601df7d86835f5fec2a21f8fc9f", "ChloePrice", "&7&oempty");
        new HatType("208bc06df75c3d21c3811783d9de5bed6a7fed65438f8e8ed8f735922869f2c3", "BigSmoke", "&7&oempty");
        new HatType("972978f25b6f3c30b4c369478e11ec2b1e9f21527277159ff76586a46d205284", "Naruto", "&7&oempty");
        new HatType("e093232aea49600b5092fc182e0d5ce8999f78045dd7bda23c65cca6fcf5cf60", "Tanjiro", "&7&oempty");
        new HatType("974f408d761cd6743d7fb41321c83b01cd953830250f50a0133bf5b31cab449f", "Nezuko", "&7&oempty");
        new HatType("823ceb714937a4db35270e53b5f5bcc4a93ee6504d75951ec2e9964c691ed218", "Zenitsu", "&7&oempty");
        new HatType("1b59f28f0dc62523b66bdbc3758e1a7ed45d3d4c55335813066841a5b7a761bf", "Venti", "&7&oempty");
        new HatType("8ef86e47554da39bcec43e064a2cbd7a926d34ad4a95969aa4faba89505a4d61", "Zhongli", "&7&oempty");
        new HatType("f6b612c1e52f8e32b09218a8c2f3f2398eb9f9405ae3d39faff6e7597b75f8d1", "Megaman", "&7&oempty");
        new HatType("41f18bac40dd382eaa2e97a99e0023dd35cf7f2948b6e3b7e8c4f4ead48e2a83", "MasterRoshi", "&7&oempty");
        new HatType("44e63fd7c48ebe7a9ffbf713b5d87abdca61c8a1df316a9f162ce5db625b3f", "Heavy", "&7&oempty");
        new HatType("aab1e2d7f6787d6a4443959e8978fe081534f3364c86539cf36b978cb76ad921", "BlueSpy", "&7&oempty");
        new HatType("a86e9462294015483e6784bae971b6ce4df552e547287dc92628113cf88923c3", "Engineer", "&7&oempty");
        new HatType("867aced8c44e157e9358490b54daabd07f4629c659b7a5145dc563a49a49e1c3", "Kratos", "&7&oempty");
        new HatType("8697f4699d915f1cb3904e781d0796dc50b63007ffc097332fdbc0f46658566f", "Teemo", "&7&oempty");
        new HatType("f3487d457f9062d787a3e6ce1c4664bf7402ec67dd111256f19b38ce4f670", "Bread", "&7&oFreshly baked");
        new HatType("955d611a878e821231749b2965708cad942650672db09e26847a88e2fac2946", "Cheese", "&7&oSay cheese!");
        new HatType("347f4f5a74c6691280cd80e7148b49b2ce17dcf64fd55368627f5d92a976a6a8", "Pancakes", "&7&oBetter with syrup!");
        new HatType("f9136514f342e7c5208a1422506a866158ef84d2b249220139e8bf6032e193", "Cake", "&7&oMmmm, cake.");
        new HatType("b592cf9f42a5a8c995968493fdd1b11e0b69aad6473ff45384abe58b7fc7c7", "Cookie", "&7&oChocolate chips included!");
        new HatType("4cc3f781c923a2887f14c1eea11050166966f2602578401f1451e6097b979df", "CandyCane", "&7&oA bit of tooth-breaking");
        new HatType("819f948d17718adace5dd6e050c586229653fef645d7113ab94d17b639cc466", "Chocolate", "&7&oDon't feed your dog with this!");
        new HatType("1ed55260dccc8da59338c75e41d544a2e1e7dbef31a69fe42c01b3298bf2d", "WhiteChocolate", "&7&oA bit more creamy");
        new HatType("cbb311f3ba1c07c3d1147cd210d81fe11fd8ae9e3db212a0fa748946c3633", "Apple", "&7&oNot the company");
        new HatType("c3fed514c3e238ca7ac1c94b897ff6711b1dbe50174afc235c8f80d029", "Melon", "&7&oDon't smash it with your head");
        new HatType("fec415d702f3292a82f1471c8794cf63122d449d28ab886d4dc58fafd66", "CarvedPumpkin", "&7&oWho turned this thing off?");
        new HatType("cbc826aaafb8dbf67881e68944414f13985064a3f8f044d8edfb4443e76ba", "Strawberry", "&7&oDo these grow on trees?");
        new HatType("e9b0e969cf3fcced36b712350ffb46d8ed761fe5efb10e3b6a9795e6656da97", "Coconut", "&7&oParadise");
        new HatType("98ced74a22021a535f6bce21c8c632b273dc2d9552b71a38d57269b3538cf", "Taco", "&7&oLet's go for some tacos!");
        new HatType("e7ba22d5df21e821a6de4b8c9d373a3aa187d8ae74f288a82d2b61f272e5", "Bacon", "&7&oEverything's better with bacon.");
        new HatType("a0eacac41a9eaf051376ef2f959701e1bbe1bf4aa6715adc34b6dc29a13ea9", "Fries", "&7&oHamburger's best friend");
        new HatType("a6ef1c25f516f2e7d6f7667420e33adcf3cdf938cb37f9a41a8b35869f569b", "Hamburger", "&7&oThe classic fastfood!");
        new HatType("1497b147cfae52205597f72e3c4ef52512e9677020e4b4fa7512c3c6acdd8c1", "Popcorn", "&7&oThe classic movie snack!");
        new HatType("d07b8c51acec2a508bb2fa652fb6e4a08b19485159a099f5982ccb88df1fe27e", "WhiteDonut", "&7&oLike a donut, but in white!");
        new HatType("837c9b82b186656e9f6363a2a1c6a4b5b93cfa9ef4dad6f16b94ebb5e362678", "PinkDonut", "&7&oHomer's favorites");
        new HatType("59da54ff366e738e31de92919986abb4d50ca944fa9926af63758b7448f18", "ChocolateDonut", "&7&oA better donut!");
        new HatType("d53c1e87e537f1ab2774ddafb83439b336f4a777b47ad82bcb30d5fcbdf9bc", "Pie", "&7&oI like pie");
        new HatType("9c60da2944a177dd08268fbec04e40812d1d929650be66529b1ee5e1e7eca", "A", "&7&oThe first letter in the alphabet!");
        new HatType("8041f5e86983d36eaec4e167b2bbb5a3727607cde88f7555ca1b522a039bb", "B", "&7&oThe second letter in the alphabet!");
        new HatType("d945996c8ae91e376196d4dc676fec31feac790a2f195b2981a703ca1d16cb6", "C", "&7&oThe third letter in the alphabet!");
        new HatType("1641150f481e8492f7128c948996254d2d91fc90f5a8ff4d8ac5c39a6a88a", "D", "&7&oThe fourth letter in the alphabet!");
        new HatType("db251487ff8eef2ebc7a57dab6e3d9f1db7fc926ddc66fea14afe3dff15a45", "E", "&7&oThe fifth letter in the alphabet!");
        new HatType("7e433656b443668ed03dac8c442722a2a41221be8bb48e23b35bd8c2e59f63", "F", "&7&oThe sixth letter in the alphabet!");
        new HatType("995863b73637605feacbb173b77d5e155e65204c78d5c7911f738f28deb60", "G", "&7&oThe seventh letter in the alphabet!");
        new HatType("3c1d358d927074289cc26bff5b1240746f9f4f0cc46f942f5981c6595f72dd", "H", "&7&oThe eighth letter in the alphabet!");
        new HatType("8f2295865bda4e47979d36b8a887a75a13b034e6988f78670b64a1e6442c", "I", "&7&oThe ninth letter in the alphabet!");
        new HatType("e34462b55d7f5823680ad13f2adbd7d1ed46ba5101017ed4b37aeeeb775d", "J", "&7&oThe tenth letter in the alphabet!");
        new HatType("773325a935c067b6ef227367f62ca4bf49f67adb9f6da32091e2d32c5dde328", "K", "&7&oThe eleventh letter in the alphabet!");
        new HatType("25a1e3328c571aa495d9c5f494815cca176c3acb184feb5a7b9c96ce8e52fce", "L", "&7&oThe twelfth letter in the alphabet!");
        new HatType("d467bf6be95e5c8e9d01977a2f0c487ed5b0de5c87963a2eb15411c442fb2b", "M", "&7&oThe thirteenth letter in the alphabet!");
        new HatType("823e434d6395fe7e63492431bdee5782bd5ee5bc8cab7559467bdd1f93b925a", "N", "&7&oThe fourteenth letter in the alphabet!");
        new HatType("88445466bdc5ad5bcea82239c4e1b510f6ea5262d82d8a96d7291c342fb89", "O", "&7&oThe fifteenth letter in the alphabet!");
        new HatType("f9de601dee3ffeca4d54595f844201d0ed2091acec4548c696bb16a8a158f6", "P", "&7&oThe sixteenth letter in the alphabet!");
        new HatType("66ca769bde25d4cc41e19e42adc35ab4c1557b76af232649acc9967ff198f13", "Q", "&7&oThe seventeenth letter in the alphabet!");
        new HatType("67a188805162ca5dd4f4649c661d3f6d23c42662aef01645b1a97f78b3f13219", "R", "&7&oThe eighteenth letter in the alphabet!");
        new HatType("60d09dfd9f5de6243233e0e3325b6c3479335e7ccf13f2448d4e1f7fc4a0df", "S", "&7&oThe nineteenth letter in the alphabet!");
        new HatType("64c75619b91d241f678350ad9237c134c5e08d87d6860741ede306a4ef91", "T", "&7&oThe twentieth letter in the alphabet!");
        new HatType("e9f6d2c6d5285f882ae55d1e91b8f9efdfc9b377208bf4c83f88dd156415e", "U", "&7&oThe twenty-first letter in the alphabet!");
        new HatType("dce27a153635f835237d85c6bf74f5b1f2e638c48fee8c83038d0558d41da7", "V", "&7&oThe twenty-second letter in the alphabet!");
        new HatType("aedcf4ffcb53b56d42baac9d0dfb118e343462327442dd9b29d49f50a7d38b", "W", "&7&oThe twenty-third letter in the alphabet!");
        new HatType("83618ff1217640bec5b525fa2a8e671c75d2a7d7cb2ddc31d79d9d895eab1", "X", "&7&oThe twenty-fourth letter in the alphabet!");
        new HatType("d9c1d29a38bcf113b7e8c34e148a79f9fe41edf41aa8b1de873bb1d433b3861", "Y", "&7&oThe twenty-fifth letter in the alphabet!");
        new HatType("b9295734195d2c7fa389b98757e9686ce6437c16c58bdf2b4cd538389b5912", "Z", "&7&oThe twenty-sixth letter in the alphabet!");

        ConfigurationSection hats = getCustomConfig(Category.HATS);
        if (hats == null) return;

        String url;
        for (String key : hats.getKeys(false)) {
            url = hats.getString(key + ".url");
            if (url == null) {
                UltraCosmeticsData.get().getPlugin().getSmartLogger().write(LogLevel.WARNING, "Incomplete custom hat '" + key + "'");
                continue;
            }
            MessageManager.addMessage(Category.HATS.getConfigPath() + "." + key + ".Name", key);
            new HatType(url, key, "A custom hat!");
        }
    }
}
