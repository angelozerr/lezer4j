package lezer.tree;

import static lezer.tree.LezerTreeAssertion.anonTree;
import static lezer.tree.LezerTreeAssertion.mk;
import static lezer.tree.LezerTreeAssertion.recur;
import static lezer.tree.LezerTreeAssertion.simple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class SyntaxNodeTest {

	@Test
	public void canResolveAtTheTopLevel() {
		SyntaxNode c = simple().resolve(2, -1);
		assertEquals(1, c.from());
		assertEquals(2, c.to());
		assertEquals("a", c.name());
		assertEquals("T", c.parent().name());
		assertNull(c.parent().parent());
		c = simple().resolve(2, 1);
		assertEquals(2, c.from());
		assertEquals(3, c.to());
		c = simple().resolve(2);
		assertEquals("T", c.name());
		assertEquals(0, c.from());
		assertEquals(23, c.to());
	}

	@Test
	public void canResolveDeeper() {
		SyntaxNode c = simple().resolve(10, 1);
		assertEquals("c", c.name());
		assertEquals(10, c.from());
		assertEquals("Br", c.parent().name());
		assertEquals("Pa", c.parent().parent().name());
		assertEquals("T", c.parent().parent().parent().name());
	}

	@Test
	public void canResolveInALargeTree() {
		SyntaxNode c = recur().resolve(10, 1);
		int depth = 1;
		while ((c = c.parent()) != null)
			depth++;
		assertEquals(depth, 8);
	}

	@Test
	public void cachesResolvedParents() {
		SyntaxNode a = recur().resolve(3, 1);
		SyntaxNode b = recur().resolve(3, 1);
		assertEquals(a, b);
	}

	@Test
	public void canGetChildrenByGroup() {
		SyntaxNode tree = mk("aa(bb)[aabbcc]").topNode();
		assertEquals("a,a", flat(tree.getChildren("atom")));
		assertEquals("", flat(tree.firstChild().getChildren("atom")));
		assertEquals("a,a,b,b,c,c", flat(tree.lastChild().getChildren("atom")));
	}

	@Test
	public void canGetSingleChildren() {
		SyntaxNode tree = mk("abc()").topNode();
		assertEquals(null, tree.getChild("Br"));
		assertEquals("Pa", tree.getChild("Pa").name());
	}

	@Test
	public void canGetChildrenBetweenOthers() {
		SyntaxNode tree = mk("aa(bb)[aabbcc]").topNode();
		assertNotNull(tree.getChild("Pa", "atom", "Br"));
		assertNull(tree.getChild("Pa", "atom", "atom"));
		SyntaxNode last = tree.lastChild();
		assertEquals("b,b", flat(last.getChildren("b", "a", "c")));
		assertEquals("a,a", flat(last.getChildren("a", null, "c")));
		assertEquals("c,c", flat(last.getChildren("c", "b", null)));
		assertEquals("", flat(last.getChildren("b", "c")));
	}

	@Test
	public void skipsAnonymousNodes() {
		assertEquals("T(a,b)", anonTree + "");
		assertEquals("T", anonTree.resolve(1).name());
		assertEquals("b", anonTree.topNode().lastChild().name());
		assertEquals("a", anonTree.topNode().firstChild().name());
		assertEquals("b", anonTree.topNode().childAfter(1).name());
	}

	private static String flat(List<SyntaxNode> children) {
		return children.stream() //
				.map(c -> c.name()) //
				.collect(Collectors.joining(","));
	}

}
